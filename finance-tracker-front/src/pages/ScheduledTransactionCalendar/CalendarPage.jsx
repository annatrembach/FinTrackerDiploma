import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useSelector } from 'react-redux';
import { FaBan, FaTrash, FaArrowLeft, FaArrowRight } from 'react-icons/fa';
import './CalendarPage.css';

const API_BASE     = 'http://localhost:8082/api/scheduled-transactions';
const CATEGORY_API = 'http://localhost:8082/api/categories';

const MONTHS       = ['January','February','March','April','May','June','July','August','September','October','November','December'];
const DAYS_OF_WEEK = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
const RECURRENCES  = ['ONCE','DAILY','WEEKLY','MONTHLY'];
const TYPES        = ['INCOME','EXPENSE'];

const currentYear = new Date().getFullYear();
const YEARS = Array.from({ length: 10 }, (_, i) => currentYear - 3 + i);

function formatStatus(s) {
  return { PENDING:'Pending', EXECUTED:'Executed', CANCELLED:'Cancelled' }[s] || s;
}

function str(val) {
  if (!val) return '';
  if (typeof val === 'object') return val.name ?? val.label ?? String(val.id ?? '');
  return String(val);
}

function buildCalendarDays(year, month) {
  const firstDay    = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const daysInPrev  = new Date(year, month, 0).getDate();
  const cells = [];
  for (let i = firstDay - 1; i >= 0; i--)
    cells.push({ day: daysInPrev - i, currentMonth: false, date: new Date(year, month - 1, daysInPrev - i) });
  for (let d = 1; d <= daysInMonth; d++)
    cells.push({ day: d, currentMonth: true, date: new Date(year, month, d) });
  const rem = 42 - cells.length;
  for (let d = 1; d <= rem; d++)
    cells.push({ day: d, currentMonth: false, date: new Date(year, month + 1, d) });
  return cells;
}

function isSameDay(a, b) {
  return a.getFullYear() === b.getFullYear() &&
         a.getMonth()    === b.getMonth()    &&
         a.getDate()     === b.getDate();
}

function resetTime(d) {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate());
}

function txOccursOnDay(tx, cellDate) {
  if (!tx.scheduledDate) return false;
  const startDate   = new Date(tx.scheduledDate);
  const startNoTime = resetTime(startDate);
  const cellNoTime  = resetTime(cellDate);
  if (cellNoTime < startNoTime) return false;
  if (isSameDay(cellDate, startDate)) return true;
  if (tx.recurrence === 'ONCE')    return false;
  if (tx.recurrence === 'DAILY')   return true;
  if (tx.recurrence === 'WEEKLY')  return cellDate.getDay()  === startDate.getDay();
  if (tx.recurrence === 'MONTHLY') return cellDate.getDate() === startDate.getDate();
  return false;
}

/* ── Category autocomplete ── */
function CategorySelect({ categories, value, onChange, token, onCategoriesReload }) {
  const [open,  setOpen]  = useState(false);
  const [input, setInput] = useState('');
  const ref = useRef();

  useEffect(() => {
    if (!value) { setInput(''); return; }
    const found = categories.find(c => c.id === value);
    setInput(found ? found.name : '');
  }, [value, categories]);

  useEffect(() => {
    const handler = e => {
      if (ref.current && !ref.current.contains(e.target)) {
        setOpen(false);
        const match = categories.find(c => c.name.toLowerCase() === input.toLowerCase());
        if (match) onChange(match.id);
        else if (!value) setInput('');
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [input, value, categories, onChange]);

  const filtered   = categories.filter(c => c.name.toLowerCase().includes(input.toLowerCase()));
  const exactMatch = categories.some(c => c.name.toLowerCase() === input.toLowerCase());

  const handleSelect = c => { onChange(c.id); setInput(c.name); setOpen(false); };

  const handleCreate = async () => {
    if (!input.trim()) return;
    try {
      const res = await fetch(CATEGORY_API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ name: input.trim() }),
      });
      if (!res.ok) throw new Error();
      const cat = await res.json();
      onCategoriesReload();
      onChange(cat.id);
      setInput(cat.name);
      setOpen(false);
    } catch {
      alert('Failed to create category');
    }
  };

  return (
    <div ref={ref} className="custom-select-with-input">
      <input
        className="custom-select-input"
        type="text"
        placeholder="Category"
        value={input}
        onChange={e => { setInput(e.target.value); setOpen(true); onChange(null); }}
        onFocus={() => setOpen(true)}
      />
      {open && (
        <ul className="custom-select-input-options">
          {filtered.map(c => (
            <li key={c.id} className="custom-select-option" onClick={() => handleSelect(c)}>{c.name}</li>
          ))}
          {!exactMatch && input.trim() && (
            <li className="custom-select-option create-option" onClick={handleCreate}>
              + Create "{input.trim()}"
            </li>
          )}
        </ul>
      )}
    </div>
  );
}

function TypeSelect({ value, onChange }) {
  const [open, setOpen] = useState(false);
  const ref = useRef();

  useEffect(() => {
    const handler = e => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleSelect = (t) => {
    onChange(t);
    setOpen(false);
  };

  // Відображаємо значення тільки якщо воно обране, інакше — порожньо
  const displayValue = value === 'INCOME' ? 'Income' : (value === 'EXPENSE' ? 'Expense' : '');

  return (
    <div ref={ref} className="custom-select-with-input">
      <input
        className="custom-select-input"
        type="text"
        readOnly
        placeholder="Type" // додано placeholder
        value={displayValue}
        onClick={() => setOpen(!open)}
        style={{ cursor: 'pointer' }}
      />
      {open && (
        <ul className="custom-select-input-options">
          {TYPES.map(t => (
            <li key={t} className="custom-select-option" onClick={() => handleSelect(t)}>
              {t.charAt(0) + t.slice(1).toLowerCase()}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function HeaderSelect({ value, options, onChange, width = '130px' }) {
  const [open, setOpen] = useState(false);
  const ref = useRef();

  useEffect(() => {
    const handler = e => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const displayLabel = options.find(opt => opt.value === value)?.label || value;

  return (
    <div ref={ref} className="header-select-container" style={{ width: width }}>
      <div className="header-select-input-wrapper" onClick={() => setOpen(!open)}>
        <input
          className="custom-select-input header-style"
          type="text"
          readOnly
          value={displayLabel}
        />
      </div>
      {open && (
        <ul className="header-select-options">
          {options.map((opt) => (
            <li 
              key={opt.value} 
              className="custom-select-option" 
              onClick={() => { onChange(opt.value); setOpen(false); }}
            >
              {opt.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function FilterSelect({ value, options, onChange, width = '150px' }) {
  const [open, setOpen] = useState(false);
  const ref = useRef();

  useEffect(() => {
    const handler = e => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const displayLabel = options.find(opt => opt.value === value)?.label || value;

  return (
    <div ref={ref} className="filter-select-container" style={{ width }}>
      <div className="filter-select-trigger" onClick={() => setOpen(!open)}>
        <span>{displayLabel}</span>
        <span className={`filter-chevron ${open ? 'open' : ''}`}>▼</span>
      </div>
      {open && (
        <ul className="filter-options-dropdown">
          {options.map((opt) => (
            <li 
              key={opt.value} 
              className="filter-option-item" 
              onClick={() => { onChange(opt.value); setOpen(false); }}
            >
              {opt.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

/* ═══════════════════════════════════════════════════════════ */
const EMPTY_FORM = {
  amount: '', type: '', categoryId: null,
  description: '', scheduledDate: '', recurrence: 'ONCE',
};

export default function CalendarPage({ isSidebarOpen }) {
  const user  = useSelector(s => s.auth.user);
  const token = useSelector(s => s.auth.jwt);

  const today = new Date();
  const [year,  setYear]  = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth());

  const [transactions, setTransactions] = useState([]);
  const [categories,   setCategories]   = useState([]);
  const [isLoading,    setIsLoading]    = useState(false);
  const [isError,      setIsError]      = useState(false);
  const [hoveredCell,  setHoveredCell]  = useState(null);
  const [form,         setForm]         = useState(EMPTY_FORM);
  const [filterField,  setFilterField]  = useState('description');
  const [filterValue,  setFilterValue]  = useState('');

  const loadCategories = useCallback(() => {
    if (!token) return;
    fetch(CATEGORY_API, { headers: { Authorization: `Bearer ${token}` } })
      .then(r => r.json())
      .then(d => setCategories(Array.isArray(d) ? d : []))
      .catch(() => {});
  }, [token]);

  const loadTransactions = useCallback(() => {
    if (!user?.id) return;
    setIsLoading(true);
    setIsError(false);
    fetch(`${API_BASE}/user/${user.id}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then(r => { if (!r.ok) throw new Error(r.status); return r.json(); })
      .then(d => { setTransactions(Array.isArray(d) ? d : []); setIsLoading(false); })
      .catch(err => { console.error(err); setIsError(true); setIsLoading(false); });
  }, [user, token]);

  useEffect(() => { loadCategories(); },   [loadCategories]);
  useEffect(() => { loadTransactions(); }, [loadTransactions]);

  const prevMonth = () => { if (month === 0) { setMonth(11); setYear(y => y-1); } else setMonth(m => m-1); };
  const nextMonth = () => { if (month === 11) { setMonth(0);  setYear(y => y+1); } else setMonth(m => m+1); };

  /* ── calendar cells ── */
  const cells = buildCalendarDays(year, month);

  /* ── txByDate map (for calendar highlighting) ── */
  const txByDate = {};
  cells.forEach(cell => {
    const key = `${cell.date.getFullYear()}-${cell.date.getMonth()}-${cell.date.getDate()}`;
    transactions.forEach(tx => {
      if (txOccursOnDay(tx, cell.date)) {
        (txByDate[key] ??= []).push(tx);
      }
    });
  });

  /* ── monthTxs list (for the card list below calendar) ── */
  const monthTxs = [];
  const lastDayOfMonth = new Date(year, month + 1, 0).getDate();
  for (let d = 1; d <= lastDayOfMonth; d++) {
    const currentDay = new Date(year, month, d);
    transactions.forEach(tx => {
      if (txOccursOnDay(tx, currentDay)) {
        monthTxs.push({ ...tx, displayDate: currentDay });
      }
    });
  }
  monthTxs.sort((a, b) => a.displayDate - b.displayDate);

  /* ── filtered list ── */
  const filteredMonthTxs = monthTxs.filter(tx => {
    if (!filterValue) return true;
    switch (filterField) {
      case 'description':
        return str(tx.description).toLowerCase().includes(filterValue.toLowerCase());
      case 'category':
        return str(tx.category).toLowerCase().includes(filterValue.toLowerCase());
      case 'status':
        return tx.status === filterValue;
      case 'amount':
        return Number(tx.amount) >= Number(filterValue);
      case 'scheduledDate':
        if (!tx.displayDate) return false;
        const d = new Date(tx.displayDate);
        // Форматуємо дату в YYYY-MM-DD за місцевим часом
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const localDateString = `${year}-${month}-${day}`;
        
        return localDateString === filterValue;
      default:
        return true;
    }
  });

  /* ── create ── */
  const handleCreate = async () => {
    if (!user?.id)                                return alert('User not logged in');
    if (!form.amount || Number(form.amount) <= 0) return alert('Amount must be positive');
    if (!form.scheduledDate)                      return alert('Please select a scheduled date');
    if (!form.categoryId)                         return alert('Please select or create a category');
    if (!token)                                   return alert('Login token missing');
    try {
      const res = await fetch(`${API_BASE}/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          amount:        Number(form.amount),
          type:          form.type,
          category:      { id: form.categoryId },
          description:   form.description,
          scheduledDate: form.scheduledDate,
          recurrence:    form.recurrence,
        }),
      });
      if (!res.ok) { const t = await res.text(); throw new Error(t); }
      setForm(EMPTY_FORM);
      loadTransactions();
    } catch (e) {
      alert('Error creating: ' + e.message);
    }
  };

  /* ── cancel ── */
  const handleCancel = id =>
    fetch(`${API_BASE}/${id}/cancel`, {
      method: 'PUT',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then(r => { if (!r.ok) throw new Error(); return r.json(); })
      .then(() => loadTransactions())
      .catch(() => alert('Error cancelling'));

      
    const handleDelete = id =>
    fetch(`${API_BASE}/delete/${id}`, {
      method: 'DELETE',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then(r => { if (!r.ok) throw new Error(); })
      .then(() => loadTransactions())
      .catch(() => alert('Error deleting'));

  /* ════════════════ render ════════════════ */
  return (
    <div className={`scheduled-container ${isSidebarOpen ? 'shifted' : ''}`}>

      {/* Header */}
      <div className="calendar-header">
        <h2>Scheduled Transactions</h2>
        <div style={{ display:'flex', alignItems:'center', gap:'1rem', flexWrap:'wrap' }}>
          <div className="calendar-picker">
            {/* Вибір місяця */}
            <HeaderSelect 
                value={month} 
                options={MONTHS.map((m, i) => ({ value: i, label: m }))}
                onChange={val => setMonth(val)}
                width="140px"
            />
            
            {/* Вибір року */}
            <HeaderSelect 
                value={year} 
                options={YEARS.map(y => ({ value: y, label: String(y) }))}
                onChange={val => setYear(val)}
                width="100px"
            />
            </div>
          <div className="calendar-nav">
            <button onClick={prevMonth}><FaArrowLeft size={15}/></button>
            <span className="month-label">{MONTHS[month]} {year}</span>
            <button onClick={nextMonth}><FaArrowRight size={15}/></button>
          </div>
        </div>
      </div>

      {/* Add form */}
      <div className="add-scheduled-form">
        <div className="form-inputs">
          <input type="number" placeholder="Amount" min="0.01" step="0.01"
            value={form.amount} onChange={e => setForm({...form, amount: e.target.value})} />

            <TypeSelect 
                value={form.type} 
                onChange={val => setForm({...form, type: val})} 
            />

          <CategorySelect
            categories={categories}
            value={form.categoryId}
            onChange={id => setForm({...form, categoryId: id})}
            token={token}
            onCategoriesReload={loadCategories}
          />

          <input type="text" placeholder="Description"
            value={form.description} onChange={e => setForm({...form, description: e.target.value})} />

          <input type="datetime-local"
            value={form.scheduledDate} onChange={e => setForm({...form, scheduledDate: e.target.value})} />
        </div>

        <div className="form-footer">
          <div className="recurrence-group">
            <span className="recurrence-label">Frequency:</span>
            <div className="recurrence-options">
              {RECURRENCES.map(r => (
                <label key={r} className={`recurrence-option ${form.recurrence === r ? 'active' : ''}`}>
                  <input
                    type="radio"
                    name="recurrence"
                    value={r}
                    checked={form.recurrence === r}
                    onChange={e => setForm({ ...form, recurrence: e.target.value })}
                  />
                  {r.charAt(0) + r.slice(1).toLowerCase()}
                </label>
              ))}
            </div>
          </div>
          <button className="add-btn" onClick={handleCreate}>+ Add Transaction</button>
        </div>
      </div>

      {/* Status messages */}
      {!user?.id && <p className="no-user-msg">Please log in to see your scheduled transactions.</p>}
      {isLoading  && <p className="loading-msg">Loading...</p>}
      {isError    && <p className="error-msg">Could not load scheduled transactions.</p>}

      {/* Calendar */}
      <div className="calendar-weekdays">
        {DAYS_OF_WEEK.map(d => <div key={d} className="calendar-weekday">{d}</div>)}
      </div>

      <div className="calendar-cells">
        {cells.map((cell, idx) => {
          const key    = `${cell.date.getFullYear()}-${cell.date.getMonth()}-${cell.date.getDate()}`;
          const dayTxs = txByDate[key] || [];
          const isToday = isSameDay(cell.date, today);

          return (
            <div
              key={idx}
              className={['cal-cell',
                !cell.currentMonth ? 'other-month'      : '',
                isToday            ? 'today'             : '',
                dayTxs.length > 0  ? 'has-transactions' : '',
              ].filter(Boolean).join(' ')}
              onMouseEnter={() => dayTxs.length > 0 && setHoveredCell(idx)}
              onMouseLeave={() => setHoveredCell(null)}
            >
              <span className="day-number">{cell.day}</span>

              {dayTxs.length > 0 && (
                <div className="tx-dots">
                  {dayTxs.slice(0,5).map((tx,i) => (
                    <span key={i} className={`tx-dot ${str(tx.type).toLowerCase()}`} />
                  ))}
                </div>
              )}

              {hoveredCell === idx && dayTxs.length > 0 && (
                <div className="tx-tooltip">
                  <h4>{cell.date.toLocaleDateString('uk-UA', {day:'numeric', month:'long'})}</h4>
                  {dayTxs.map((tx,i) => (
                    <div key={i} className="tx-tooltip-item">
                      <span className={`txi-amount ${str(tx.type).toLowerCase()}`}>
                        {str(tx.type) === 'INCOME' ? '+' : '-'}{tx.amount}
                      </span>
                      {tx.category    && <span> · {str(tx.category)}</span>}
                      {tx.description && <span> — {str(tx.description)}</span>}
                      <span className={`status-badge ${tx.status}`}>{formatStatus(tx.status)}</span>
                      <br/>
                      <span style={{fontSize:'0.71rem', color:'#aaa'}}>{str(tx.recurrence)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Monthly list with filter */}
      <div className="scheduled-list">
        <h3>{MONTHS[month]} {year} — All Scheduled</h3>

        <div className="filter-bar">
        {/* Ліва частина: вибір поля фільтрації */}
        <FilterSelect
            value={filterField}
            options={[
            { value: 'description', label: 'Description' },
            { value: 'category', label: 'Category' },
            { value: 'status', label: 'Status' },
            { value: 'amount', label: 'Amount ≥' },
            { value: 'scheduledDate', label: 'Date' },
            ]}
            onChange={val => { setFilterField(val); setFilterValue(''); }}
            width="150px"
        />

        <div className="filter-value-wrap">
            {filterField === 'status' && (
            <FilterSelect
                value={filterValue}
                options={[
                { value: '', label: 'All statuses' },
                { value: 'PENDING', label: 'Pending' },
                { value: 'EXECUTED', label: 'Executed' },
                { value: 'CANCELLED', label: 'Cancelled' },
                ]}
                onChange={setFilterValue}
                width="100%"
            />
            )}
            
            {filterField === 'category' && (
            <FilterSelect
                value={filterValue}
                options={[
                { value: '', label: 'All categories' },
                ...categories.map(c => ({ value: c.name, label: c.name }))
                ]}
                onChange={setFilterValue}
                width="100%"
            />
            )}

            {filterField === 'scheduledDate' && (
            <input type="date" className="filter-value-input" value={filterValue} onChange={e => setFilterValue(e.target.value)} />
            )}
            
            {filterField === 'amount' && (
            <input type="number" className="filter-value-input" placeholder="Min amount..." value={filterValue} onChange={e => setFilterValue(e.target.value)} />
            )}
            
            {filterField === 'description' && (
            <input type="text" className="filter-value-input" placeholder="Search description..." value={filterValue} onChange={e => setFilterValue(e.target.value)} />
            )}

            {filterValue && (
            <button className="filter-clear-btn" onClick={() => setFilterValue('')} title="Clear">✕</button>
            )}
        </div>
        </div>

        {filteredMonthTxs.length === 0
          ? <p style={{color:'#aaa', fontSize:'0.9rem'}}>No scheduled transactions this month.</p>
          : (
            <div className="scheduled-cards">
              {filteredMonthTxs.map((tx, idx) => (
                <div key={`${tx.id}-${idx}`} className="scheduled-card">
                  <span className="sc-date">
                    {new Date(tx.displayDate).toLocaleDateString('uk-UA')}
                  </span>
                  <span className={`sc-amount ${str(tx.type).toLowerCase()}`}>
                    {str(tx.type) === 'INCOME' ? '+' : '-'}{tx.amount}
                  </span>
                  <div className="sc-meta">
                    {tx.category    && <span>{str(tx.category)}</span>}
                    {tx.description && <span>{str(tx.description)}</span>}
                  </div>
                  <span className="sc-recurrence">{str(tx.recurrence)}</span>
                  <span className={`status-badge ${tx.status}`}>{formatStatus(tx.status)}</span>
                  <div className="sc-actions">
                    <button onClick={() => handleCancel(tx.id)} disabled={tx.status !== 'PENDING'} title="Cancel">
                      <FaBan />
                    </button>
                    <button onClick={() => handleDelete(tx.id)} title="Remove">
                      <FaTrash />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )
        }
      </div>
    </div>
  );
}