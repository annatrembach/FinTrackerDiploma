import React, { useState } from "react";
import AIChat from "./AIChat";
import AIAnalysis from "./AIAnalysis";
import AIDoc from "./AIDoc";
import "./AIAssistancePage.css";

const AIAssistancePage = ({ isSidebarOpen }) => {
  const [activeTab, setActiveTab] = useState("chat");

  return (
    <div className={`ai-page ${isSidebarOpen ? "shifted" : ""}`}>
      <h2 className="ai-title">AI Assistance</h2>

      {/* Tabs */}
      <div className="ai-tabs">
        <button
          className={activeTab === "chat" ? "active" : ""}
          onClick={() => setActiveTab("chat")}
        >
          Chat
        </button>

        <button
          className={activeTab === "analysis" ? "active" : ""}
          onClick={() => setActiveTab("analysis")}
        >
          Analysis
        </button>

        <button
          className={activeTab === "doc" ? "active" : ""}
          onClick={() => setActiveTab("doc")}
        >
          Docs
        </button>
      </div>

      {/* Content */}
      <div className="ai-content">
        {activeTab === "chat" && <AIChat isSidebarOpen={isSidebarOpen} />}
        {activeTab === "analysis" && <AIAnalysis />}
        {activeTab === "doc" && <AIDoc />}
      </div>
    </div>
  );
};

export default AIAssistancePage;