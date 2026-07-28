import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import ReactMarkdown from "react-markdown";
import "./AIDocChat.css";

const SUGGESTIONS = [
  "How do I add a transaction?",
  "What are financial goals?",
  "How does the AI analysis work?",
  "How to set a savings target?",
];

const formatTime = () =>
  new Date().toLocaleTimeString("en", { hour: "2-digit", minute: "2-digit" });

const AIDoc = ({ isSidebarOpen }) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);
  const textareaRef = useRef(null);
  const jwt = localStorage.getItem("jwt");

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const handleInput = (e) => {
    setInput(e.target.value);
    const ta = textareaRef.current;
    if (ta) {
      ta.style.height = "auto";
      ta.style.height = ta.scrollHeight + "px";
    }
  };

  const send = async (question = input) => {
    const text = question.trim();
    if (!text || loading) return;

    setInput("");
    if (textareaRef.current) textareaRef.current.style.height = "auto";

    const userMsg = { sender: "user", text, time: formatTime() };
    setMessages((prev) => [...prev, userMsg]);
    setLoading(true);

    try {
      const res = await axios.post(
        "http://localhost:8084/api/ai/doc",
        { question: text },
        { headers: { Authorization: `Bearer ${jwt}` } }
      );
      setMessages((prev) => [
        ...prev,
        { sender: "ai", text: res.data, time: formatTime() },
      ]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { sender: "ai", text: "Error: AI service unavailable", time: formatTime() },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  const clearChat = () => setMessages([]);

  return (
    <div className={`doc-container ${isSidebarOpen ? "shifted" : ""}`}>
      <h2 className="doc-title">Documentation Assistant</h2>

      <div className="doc-box">
        {messages.length === 0 ? (
          <div className="doc-empty">
            <p className="doc-empty-title">Ask anything about the app</p>
            <div className="doc-suggestions">
              {SUGGESTIONS.map((s) => (
                <button key={s} className="sug-btn" onClick={() => send(s)}>
                  {s}
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg, i) => (
            <div key={i} className={`doc-message ${msg.sender}`}>
              <div className="msg-content markdown-content">
                <ReactMarkdown>
                  {msg.text}
                </ReactMarkdown>
              </div>
              <div className="msg-time">{msg.time}</div>
            </div>
          ))
        )}
        {loading && (
          <div className="doc-message ai loading">
            <div className="typing-dots">
              <span>.</span><span>.</span><span>.</span>
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="doc-footer-meta">
        <span className="rag-badge">RAG · docs search</span>
        {messages.length > 0 ? (
          <button className="clear-btn" onClick={clearChat}>Clear chat</button>
        ) : (
          <span />                   
        )}
      </div>

      <div className="doc-input-area">
        <textarea
          ref={textareaRef}
          className="doc-textarea"
          placeholder="Type your question..."
          value={input}
          onChange={handleInput}
          onKeyDown={handleKeyDown}
          rows={1}
          disabled={loading}
        />
        <button className="doc-send-btn" onClick={() => send()} disabled={loading}>
          Send
        </button>
      </div>
    </div>
  );
};

export default AIDoc;