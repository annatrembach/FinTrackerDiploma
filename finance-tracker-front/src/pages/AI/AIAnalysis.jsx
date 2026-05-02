import React, { useState } from "react";
import axios from "axios";
import "./AIAnalysis.css";

// ─── helpers ────────────────────────────────────────────────────────────────

const getScoreInfo = (score) => {
  switch (score) {
    case "GOOD":            return { label: "Good",            cls: "badge-good" };
    case "NEEDS_ATTENTION": return { label: "Needs attention", cls: "badge-danger" };
    default:                return { label: "Moderate",        cls: "badge-moderate" };
  }
};

const trendArrow = (t) => {
  if (t.direction === "STABLE") return { sym: "→", cls: "arrow-stable" };
  const isIncome = t.category.toLowerCase().includes("income");
  if (t.direction === "UP")
    return isIncome ? { sym: "↑", cls: "arrow-up-good" } : { sym: "↑", cls: "arrow-up-bad" };
  return isIncome ? { sym: "↓", cls: "arrow-up-bad" } : { sym: "↓", cls: "arrow-up-good" };
};

const fmtMonthYear = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr + "-01");
  return d.toLocaleString("en", { month: "long", year: "numeric" });
};

// ─── period toolbar ──────────────────────────────────────────────────────────

const PRESETS = [
  {
    label: "This month",
    getPeriod: () => {
      const d = new Date();
      return d.toLocaleString("en", { month: "long", year: "numeric" });
    },
  },
  {
    label: "Last month",
    getPeriod: () => {
      const d = new Date();
      d.setMonth(d.getMonth() - 1);
      return d.toLocaleString("en", { month: "long", year: "numeric" });
    },
  },
  {
    label: "Last 3 months",
    getPeriod: () => buildRange(3),
  },
  {
    label: "Last 6 months",
    getPeriod: () => buildRange(6),
  },
  {
    label: "This year",
    getPeriod: () => String(new Date().getFullYear()),
  },
];

function buildRange(n) {
  const to   = new Date();
  const from = new Date();
  from.setMonth(from.getMonth() - (n - 1));
  const fmt = (d) => d.toLocaleString("en", { month: "long", year: "numeric" });
  return `${fmt(from)} – ${fmt(to)}`;
}

function toMonthInput(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

const PeriodToolbar = ({ onAnalyze, loading }) => {
  const now  = new Date();
  const prev = new Date();
  prev.setMonth(prev.getMonth() - 1);

  const [activePreset, setActivePreset] = useState(0);
  const [fromMonth,    setFromMonth]    = useState(toMonthInput(prev));
  const [toMonth,      setToMonth]      = useState(toMonthInput(now));

  const handlePreset = (i) => {
    setActivePreset(i);
  };

  const handleCustomChange = () => {
    setActivePreset(-1);
  };

  const buildPeriod = () => {
    if (activePreset >= 0) return PRESETS[activePreset].getPeriod();
    if (!fromMonth || !toMonth) return null;
    const from = fmtMonthYear(fromMonth);
    const to   = fmtMonthYear(toMonth);
    return fromMonth === toMonth ? from : `${from} – ${to}`;
  };

  const handleClick = () => {
    const period = buildPeriod();
    if (!period) return;
    onAnalyze(period);
  };

  return (
    <div className="toolbar">
      <div className="field">
        <span className="field-label">Quick period</span>
        <div className="presets">
          {PRESETS.map((p, i) => (
            <button
              key={i}
              className={`preset-btn${activePreset === i ? " active" : ""}`}
              onClick={() => handlePreset(i)}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>

      <div className="field">
        <span className="field-label">Custom range</span>
        <div className="date-row">
          <input
            type="month"
            className="date-input"
            value={fromMonth}
            onChange={(e) => { setFromMonth(e.target.value); handleCustomChange(); }}
          />
          <span className="date-sep">—</span>
          <input
            type="month"
            className="date-input"
            value={toMonth}
            onChange={(e) => { setToMonth(e.target.value); handleCustomChange(); }}
          />
        </div>
      </div>

      <button className="analyze-btn" onClick={handleClick} disabled={loading}>
        {loading ? "Analysing…" : "Analyse"}
      </button>
    </div>
  );
};

// ─── dashboard result ────────────────────────────────────────────────────────

const Dashboard = ({ data, period }) => {
  const d  = data;
  const gc = d.goalComparison;
  const onPct   = Math.round((gc.onTrackGoals / gc.totalGoals) * 100) || 0;
  const riskPct = Math.round((gc.atRiskGoals  / gc.totalGoals) * 100) || 0;
  const info    = getScoreInfo(d.overallHealthScore);

  return (
    <>
      {/* HEADER */}
      <div className="header-card">
        <div className="ai-label">AI analysis · {period}</div>
        <div className="header-top">
          <span className={`badge ${info.cls}`}>
            <span className="score-dot" />
            {info.label}
          </span>
        </div>
        <p className="summary-text">🤖 {d.summary}</p>
      </div>

      {/* TRENDS + GOALS */}
      <div className="grid2">
        <div className="card">
          <div className="card-title">Trends</div>
          {d.trends.length === 0 && (
            <p className="empty-msg">No trend data for this period.</p>
          )}
          {d.trends.map((t, i) => {
            const a = trendArrow(t);
            return (
              <div key={i} className="trend-item">
                <div className={`trend-arrow ${a.cls}`}>{a.sym}</div>
                <div style={{ flex: 1 }}>
                  <div className="trend-row">
                    <span className="trend-cat">{t.category}</span>
                    <span className="trend-pct">{t.changePercent}%</span>
                  </div>
                  <div className="trend-desc">{t.description}</div>
                </div>
              </div>
            );
          })}
        </div>

        <div className="card">
          <div className="card-title">Goal tracker</div>
          <div className="goal-stats">
            {[
              { num: gc.totalGoals,   label: "Total" },
              { num: gc.onTrackGoals, label: "On track" },
              { num: gc.atRiskGoals,  label: "At risk", danger: true },
            ].map((s, i) => (
              <div key={i} className="gstat">
                <div className={`gstat-num${s.danger ? " at-risk" : ""}`}>{s.num}</div>
                <div className="gstat-label">{s.label}</div>
              </div>
            ))}
          </div>
          {[
            { label: "On track", pct: onPct,   cls: "progress-fill-good" },
            { label: "At risk",  pct: riskPct, cls: "progress-fill-risk"  },
          ].map((row) => (
            <div key={row.label} className="progress-row">
              <div className="progress-label">
                <span>{row.label}</span>
                <span>{row.pct}%</span>
              </div>
              <div className="progress-track">
                <div className={row.cls} style={{ width: `${row.pct}%` }} />
              </div>
            </div>
          ))}
          <div className="goal-summary">{gc.summary}</div>
        </div>
      </div>

      {/* ANOMALIES */}
      {d.anomalies?.length > 0 && (
        <div className="alert-card">
          <div className="alert-title">⚠ Anomalies detected</div>
          {d.anomalies.map((a, i) => (
            <div key={i} className="alert-item">
              {typeof a === "string"
                ? a
                : `${a.description}${a.amount ? ` — ${a.amount}` : ""}`}
            </div>
          ))}
        </div>
      )}

      {/* TIPS */}
      {d.savingTips?.length > 0 && (
        <div>
          <div className="section-label">Saving tips</div>
          <div className="tips-list">
            {d.savingTips.map((tip, i) => (
              <div key={i} className="tip-item">
                <div className="tip-icon">{i + 1}</div>
                <div className="tip-text">{tip}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  );
};

// ─── main component ──────────────────────────────────────────────────────────

const AIAnalysis = () => {
  const [data,    setData]    = useState(null);
  const [period,  setPeriod]  = useState(null);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  const jwt = localStorage.getItem("jwt");

  const handleAnalyze = async (selectedPeriod) => {
    setLoading(true);
    setError(null);
    setData(null);
    try {
      const res = await axios.post(
        "http://localhost:8084/api/ai/analyze",
        { period: selectedPeriod },
        { headers: { Authorization: `Bearer ${jwt}` } }
      );
      setData(res.data);
      setPeriod(selectedPeriod);
    } catch {
      setError("Could not load analysis. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dash">
      <div className="dash-title">AI Financial Analysis</div>

      <PeriodToolbar onAnalyze={handleAnalyze} loading={loading} />

      {!loading && !data && !error && (
        <div className="dash-state">Select a period above and click Analyse.</div>
      )}
      {loading && (
        <div className="dash-state">Analysing your finances for <strong>{period ?? "selected period"}</strong>…</div>
      )}
      {error && (
        <div className="dash-state error">{error}</div>
      )}
      {data && !loading && (
        <Dashboard data={data} period={period} />
      )}
    </div>
  );
};

export default AIAnalysis;