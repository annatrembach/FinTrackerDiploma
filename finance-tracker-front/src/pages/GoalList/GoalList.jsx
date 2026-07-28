import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import { FaEdit, FaTrash, FaCheck } from 'react-icons/fa';
import './GoalListStyle.css';

const API_BASE = 'http://localhost:8083/api/goals';

const formatStatus = (status) => {
  const map = { IN_PROGRESS: 'In progress', COMPLETED: 'Completed' };
  return map[status] || status;
};

/* ── FilterSelect — як у CalendarPage ── */
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
          {options.map(opt => (
            <li
              key={String(opt.value)}
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

/* ══════════════════════════════════════════════════════════ */
export default function GoalsList({ isSidebarOpen }) {
  const user  = useSelector(state => state.auth.user);
  const token = useSelector(state => state.auth.jwt);

  /* статус-фільтр (кнопки) — як було */
  const [filter, setFilter] = useState('');

  const [goals,       setGoals]       = useState([]);
  const [isLoading,   setIsLoading]   = useState(false);
  const [isError,     setIsError]     = useState(false);
  const [editingGoal, setEditingGoal] = useState(null);

  const [newGoalName,         setNewGoalName]         = useState('');
  const [newGoalTargetAmount, setNewGoalTargetAmount] = useState('');
  const [newGoalDeadline,     setNewGoalDeadline]     = useState('');

  /* новий filter-bar */
  const [filterField, setFilterField] = useState('name');
  const [filterValue, setFilterValue] = useState('');

  /* ── loader ── */
  const loadGoals = () => {
    setIsLoading(true);
    setIsError(false);
    const url = filter ? `${API_BASE}/?goalStatus=${filter}` : `${API_BASE}/`;
    fetch(url)
      .then(res => res.json())
      .then(data => { setGoals(Array.isArray(data) ? data : []); setIsLoading(false); })
      .catch(() => { setIsError(true); setIsLoading(false); });
  };

  useEffect(() => { loadGoals(); }, [filter]);

  /* ── фронтова фільтрація (filter-bar) ── */
  const filteredGoals = goals.filter(g => {
    if (!filterValue) return true;
    switch (filterField) {
      case 'name':
        return (g.name || '').toLowerCase().includes(filterValue.toLowerCase());
      case 'amountMin':
        return Number(g.targetAmount) >= Number(filterValue);
      case 'deadline':
        return (g.deadline || '').slice(0, 10) >= filterValue;
      default:
        return true;
    }
  });

  /* ── CRUD ── */
  const handleCreate = () => {
    if (!user?.id) return alert('User not logged in');
    if (!newGoalName.trim()) return alert('Name is required');
    if (!newGoalTargetAmount || Number(newGoalTargetAmount) <= 0) return alert('Amount should be positive');
    if (!newGoalDeadline) return alert('Please select a deadline date');

    fetch(`${API_BASE}/create`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: newGoalName.trim(),
        targetAmount: Number(newGoalTargetAmount),
        currentAmount: 0,
        deadline: newGoalDeadline,
        userId: user.id,
        status: 'IN_PROGRESS',
      }),
    })
      .then(res => { if (!res.ok) throw new Error('Failed to create goal'); return res.json(); })
      .then(() => {
        setNewGoalName(''); setNewGoalTargetAmount(''); setNewGoalDeadline('');
        loadGoals();
      })
      .catch(() => alert('Error creating goal'));
  };

  const handleUpdate = () => {
    if (!user?.id)              return alert('User not logged in');
    if (!editingGoal)           return alert('No goal selected for editing');
    if (!editingGoal.name?.trim()) return alert('Name is required');
    if (!editingGoal.targetAmount || Number(editingGoal.targetAmount) <= 0) return alert('Target amount should be positive');
    if (editingGoal.currentAmount < 0) return alert('Current amount cannot be negative');
    if (editingGoal.currentAmount > editingGoal.targetAmount) return alert('Current amount cannot be greater than target amount');
    if (!editingGoal.deadline)  return alert('Deadline is required');
    if (!token)                 return alert('User token missing, please login again.');

    fetch(`${API_BASE}/update/${editingGoal.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        id:            editingGoal.id,
        name:          editingGoal.name.trim(),
        targetAmount:  Number(editingGoal.targetAmount),
        currentAmount: Number(editingGoal.currentAmount),
        deadline:      editingGoal.deadline?.slice(0, 10) || null,
        status:        editingGoal.status,
        userId:        user.id,
      }),
    })
      .then(res => { if (!res.ok) return res.json().then(err => Promise.reject(err)); return res.json(); })
      .then(() => { setEditingGoal(null); loadGoals(); })
      .catch(err => alert('Error updating goal: ' + (err.message || JSON.stringify(err))));
  };

  const handleDelete = id => {
    fetch(`${API_BASE}/delete/${id}`, { method: 'DELETE' })
      .then(() => loadGoals())
      .catch(() => alert('Error deleting goal'));
  };

  const handleCompleteGoal = id => {
    fetch(`${API_BASE}/${id}/complete`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
    })
      .then(res => { if (!res.ok) throw new Error(); return res.json(); })
      .then(() => loadGoals())
      .catch(() => alert('Error completing goal'));
  };

  /* ════════════════ render ════════════════ */
  return (
    <div className={`goals-container ${isSidebarOpen ? 'shifted' : ''}`}>
      <h2 style={{ color: '#dd757e', fontSize: '35px', fontWeight: 'bolder' }}>User Financial Goals</h2>

      {/* ── Кнопки статусу — як було ── */}
      <div className="filter-buttons">
        <button className={filter === '' ? 'active' : ''}            onClick={() => setFilter('')}>All</button>
        <button className={filter === 'IN_PROGRESS' ? 'active' : ''} onClick={() => setFilter('IN_PROGRESS')}>In progress</button>
        <button className={filter === 'COMPLETED' ? 'active' : ''}   onClick={() => setFilter('COMPLETED')}>Completed</button>
      </div>

      {/* ── Add form ── */}
      <div className="new-goal-form">
        <input type="text"   placeholder="Name"          value={newGoalName}         onChange={e => setNewGoalName(e.target.value)} />
        <input type="number" placeholder="Target amount" value={newGoalTargetAmount} onChange={e => setNewGoalTargetAmount(e.target.value)} min="1" />
        <input type="date"                               value={newGoalDeadline}     onChange={e => setNewGoalDeadline(e.target.value)} />
        <button onClick={handleCreate}>Add</button>
      </div>

      {/* ── Filter bar (між формою і таблицею) ── */}
      <div className="filter-bar">
        <FilterSelect
          value={filterField}
          options={[
            { value: 'name',      label: 'Name'       },
            { value: 'amountMin', label: 'Amount ≥'   },
            { value: 'deadline',  label: 'Deadline ≥' },
          ]}
          onChange={val => { setFilterField(val); setFilterValue(''); }}
          width="150px"
        />

        <div className="filter-value-wrap">
          {filterField === 'name' && (
            <input
              type="text"
              className="filter-value-input"
              placeholder="Search name..."
              value={filterValue}
              onChange={e => setFilterValue(e.target.value)}
            />
          )}

          {filterField === 'amountMin' && (
            <input
              type="number"
              className="filter-value-input"
              placeholder="Min amount..."
              value={filterValue}
              onChange={e => setFilterValue(e.target.value)}
            />
          )}

          {filterField === 'deadline' && (
            <input
              type="date"
              className="filter-value-input"
              value={filterValue}
              onChange={e => setFilterValue(e.target.value)}
            />
          )}

          {filterValue && (
            <button className="filter-clear-btn" onClick={() => setFilterValue('')} title="Clear">
              ✕
            </button>
          )}
        </div>
      </div>

      {/* ── Table ── */}
      {isLoading ? (
        <p>Loading...</p>
      ) : isError ? (
        <p>Error loading goals</p>
      ) : filteredGoals.length === 0 ? (
        <p>No goals found.</p>
      ) : (
        <table className="goals-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Target</th>
              <th>Current</th>
              <th>Deadline</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredGoals.map(goal =>
              editingGoal?.id === goal.id ? (
                <tr key={goal.id} className="editing">
                  <td><input type="text"   value={editingGoal.name}               onChange={e => setEditingGoal({ ...editingGoal, name: e.target.value })} /></td>
                  <td><input type="number" value={editingGoal.targetAmount}       onChange={e => setEditingGoal({ ...editingGoal, targetAmount: Number(e.target.value) })} min={0} /></td>
                  <td><input type="number" value={editingGoal.currentAmount ?? 0} onChange={e => setEditingGoal({ ...editingGoal, currentAmount: Number(e.target.value) })} min={0} max={editingGoal.targetAmount} /></td>
                  <td><input type="date"   value={editingGoal.deadline?.slice(0, 10)} onChange={e => setEditingGoal({ ...editingGoal, deadline: e.target.value })} /></td>
                  <td>{formatStatus(editingGoal.status)}</td>
                  <td>
                    <button onClick={handleUpdate}>Save</button>
                    <button onClick={() => setEditingGoal(null)}>Cancel</button>
                  </td>
                </tr>
              ) : (
                <tr key={goal.id} className={goal.status === 'COMPLETED' ? 'completed' : ''}>
                  <td>{goal.name}</td>
                  <td>{goal.targetAmount}</td>
                  <td>{goal.currentAmount ?? 0}</td>
                  <td>{goal.deadline}</td>
                  <td>{formatStatus(goal.status)}</td>
                  <td>
                    {goal.status !== 'COMPLETED' && (
                      <button onClick={() => handleCompleteGoal(goal.id)} title="Complete"><FaCheck /></button>
                    )}
                    <button onClick={() => setEditingGoal({ ...goal })} title="Edit"><FaEdit /></button>
                    <button onClick={() => handleDelete(goal.id)} title="Delete"><FaTrash /></button>
                  </td>
                </tr>
              )
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}