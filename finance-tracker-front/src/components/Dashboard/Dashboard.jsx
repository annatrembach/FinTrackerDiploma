import React, { useEffect, useState } from 'react';
import { Line, Pie, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  ArcElement, CategoryScale, LinearScale,
  PointElement, LineElement, BarElement,
  Title, Tooltip, Legend
} from 'chart.js';
import { Grid } from '@mui/material';
import './DashboardStyle.css';
import { useSelector } from 'react-redux';
import axios from 'axios';
import dayjs from 'dayjs';

ChartJS.register(
  ArcElement, CategoryScale, LinearScale,
  PointElement, LineElement, BarElement,
  Title, Tooltip, Legend
);

const TRANSACTIONS_API = 'http://localhost:8082/api/transactions';
const GOALS_API        = 'http://localhost:8083/api/goals';

const formatType = (type) => type === 'INCOME' ? 'Income' : 'Expense';

const Dashboard = ({ isSidebarOpen }) => {
  const [summary,              setSummary]              = useState({ balance: 0, income: 0, expenses: 0 });
  const [expenseCategoriesData, setExpenseCategoriesData] = useState({ labels: [], datasets: [{ data: [], backgroundColor: [], hoverBackgroundColor: [] }] });
  const [incomeVsExpensesData, setIncomeVsExpensesData] = useState({ labels: [], datasets: [] });
  const [recentTransactions,   setRecentTransactions]   = useState([]);
  const [goals,                setGoals]                = useState([]);
  const [goalsChartData,       setGoalsChartData]       = useState({ labels: [], datasets: [] });

  const jwt   = useSelector(state => state.auth.jwt) || localStorage.getItem('jwt');
  const user  = useSelector(state => state.auth.user);

  const headers = { Authorization: `Bearer ${jwt}`, Accept: 'application/json' };

  /* ── summary ── */
  useEffect(() => {
    if (!user?.id) return;
    axios.get(`${TRANSACTIONS_API}/balance/${user.id}`, { headers })
      .then(r => setSummary(r.data))
      .catch(console.error);
  }, [jwt, user]);

  /* ── expense pie ── */
useEffect(() => {
  if (!user?.id) return;
  axios.get(`${TRANSACTIONS_API}/filter`, { headers, params: { type: 'expense' } })
    .then(r => {
      const transactions = r.data;
      const categorySums = {};
      transactions.forEach(t => {
        const cat = t.category?.name || 'Other';
        categorySums[cat] = (categorySums[cat] || 0) + t.amount;
      });
      const labels = Object.keys(categorySums);
      const data   = Object.values(categorySums);
      
      // Масив ваших основних кольорів
      const colors = ['#45AAF2','#44a8a8', '#e96675', '#778BEB', '#FED330', '#D1D8E0'];
      const backgroundColors = labels.map((_, i) => colors[i % colors.length]);

      setExpenseCategoriesData({
        labels,
        datasets: [{ 
          data, 
          backgroundColor: backgroundColors,
          // ДОДАЙТЕ ЦЕЙ РЯДОК:
          hoverBackgroundColor: backgroundColors, 
          // Також можна додати ці параметри для кращого вигляду:
          hoverOffset: 10, // сектор буде трохи "вистрибувати" при наведенні
          borderWidth: 2,
          borderColor: '#ffffff',
        }]
      });
    })
    .catch(console.error);
}, [jwt, user]);

  /* ── monthly line ── */
  useEffect(() => {
    if (!user?.id) return;
    axios.get(`${TRANSACTIONS_API}/monthly-summary/${user.id}`, { headers })
      .then(r => {
        const s = r.data;
        setIncomeVsExpensesData({
          labels: s.map(item => dayjs(item.month + '-01').format('MM.YYYY')),
          datasets: [
            { label: 'Income',   data: s.map(i => i.income),  fill: false, borderColor: '#36A2EB', tension: 0.3 },
            { label: 'Expenses', data: s.map(i => i.expense), fill: false, borderColor: '#FF6384', tension: 0.3 },
          ],
        });
      })
      .catch(console.error);
  }, [jwt, user]);

  /* ── recent 10 transactions ── */
  useEffect(() => {
    if (!user?.id) return;
    axios.get(`${TRANSACTIONS_API}/filter?`, { headers })
      .then(r => {
        const all = Array.isArray(r.data) ? r.data : [];
        const sorted = [...all].sort((a, b) =>
          new Date(b.transactionDate) - new Date(a.transactionDate)
        );
        setRecentTransactions(sorted.slice(0, 10));
      })
      .catch(console.error);
  }, [jwt, user]);

  /* ── goals ── */
  useEffect(() => {
    axios.get(`${GOALS_API}/`, { headers })
      .then(r => {
        const data = Array.isArray(r.data) ? r.data : [];
        setGoals(data);

        const active = data.filter(g => g.status !== 'COMPLETED').slice(0, 6);
        setGoalsChartData({
          labels: active.map(g => g.name),
          datasets: [
            {
              label: 'Saved',
              data: active.map(g => g.currentAmount || 0),
              backgroundColor: '#dd757e',
              borderRadius: 6,
            },
            {
              label: 'Target',
              data: active.map(g => g.targetAmount),
              backgroundColor: '#f5b8bc',
              borderRadius: 6,
            },
          ],
        });
      })
      .catch(console.error);
  }, [jwt, user]);

  const totalGoals     = goals.length;
  const completedGoals = goals.filter(g => g.status === 'COMPLETED').length;
  const activeGoals    = goals.filter(g => g.status !== 'COMPLETED').length;
  const totalSaved     = goals.reduce((s, g) => s + (g.currentAmount || 0), 0);
  const totalTarget    = goals.reduce((s, g) => s + (g.targetAmount || 0), 0);
  const overallPct     = totalTarget > 0 ? Math.round((totalSaved / totalTarget) * 100) : 0;

  /* ════════════════ render ════════════════ */
  return (
    <div className={`dashboard-container ${isSidebarOpen ? 'shifted' : ''}`}>
      <div className="dashboard-inner">

        {/* ── Summary cards ── */}
        <div className="dash-cards">
          <div className="card">
            <h3 className="h3-dashboard">Total Balance</h3>
            <p className="card-value balance">${summary.balance}</p>
          </div>
          <div className="card">
            <h3 className="h3-dashboard">Income</h3>
            <p className="card-value income">${summary.income}</p>
          </div>
          <div className="card">
            <h3 className="h3-dashboard">Expenses</h3>
            <p className="card-value expense">${summary.expenses}</p>
          </div>
        </div>

        {/* ── Row 1: Pie + Line ── */}
        <div className="dash-row">
          <div className="chart-round">
            <h3 className="h3-dashboard">Expenses by Category</h3>
            <Pie className="canvas" data={expenseCategoriesData} />
          </div>
          <div className="chart-line">
            <h3 className="h3-dashboard">Income vs Expenses</h3>
            <Line data={incomeVsExpensesData} />
          </div>
        </div>

        {/* ── Row 2: Recent transactions + Goals analytics ── */}
        <div className="dash-row">

          {/* Recent transactions */}
          <div className="dash-panel recent-tx">
            <h3 className="h3-dashboard">Recent Transactions</h3>
            {recentTransactions.length === 0 ? (
              <p className="dash-empty">No transactions yet.</p>
            ) : (
              <table className="dash-tx-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Category</th>
                    <th>Description</th>
                    <th>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {recentTransactions.map(t => (
                    <tr key={t.id}>
                      <td className="tx-date">{t.transactionDate}</td>
                      <td>{t.category?.name || '—'}</td>
                      <td className="tx-desc">{t.description || '—'}</td>
                      <td className={`tx-amount ${t.type === 'INCOME' ? 'income' : 'expense'}`}>
                        {t.type === 'INCOME' ? '+' : '-'}${t.amount}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {/* Goals analytics */}
          <div className="dash-panel goals-panel">
            <h3 className="h3-dashboard">Goals Overview</h3>

            <div className="goals-stats">
              <div className="goal-stat">
                <span className="goal-stat-num">{totalGoals}</span>
                <span className="goal-stat-label">Total</span>
              </div>
              <div className="goal-stat">
                <span className="goal-stat-num active">{activeGoals}</span>
                <span className="goal-stat-label">Active</span>
              </div>
              <div className="goal-stat">
                <span className="goal-stat-num completed">{completedGoals}</span>
                <span className="goal-stat-label">Done</span>
              </div>
              <div className="goal-stat">
                <span className="goal-stat-num">{overallPct}%</span>
                <span className="goal-stat-label">Overall</span>
              </div>
            </div>

            {/* Overall progress bar */}
            <div className="overall-progress">
              <div className="progress-labels">
                <span>Saved: ${totalSaved}</span>
                <span>Target: ${totalTarget}</span>
              </div>
              <div className="progress-bar-bg">
                <div className="progress-bar-fill" style={{ width: `${overallPct}%` }} />
              </div>
            </div>

            {/* Goals bar chart */}
            {goalsChartData.labels.length > 0 && (
              <div className="goals-chart-wrap">
                <Bar
                  data={goalsChartData}
                  options={{
                    responsive: true,
                    plugins: { legend: { position: 'bottom' } },
                    scales: {
                      x: { ticks: { font: { size: 11 } } },
                      y: { beginAtZero: true },
                    },
                  }}
                />
              </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
};

export default Dashboard;