import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import "./AIDocChat.css";

const SUGGESTIONS = [
  "Hi, I want to add a transction",
  "I want to add a goal!",
  "Show me my current balance",
  "Show me all my goals",
];

const formatTime = () =>
  new Date().toLocaleTimeString("en", { hour: "2-digit", minute: "2-digit" });

const AIChat = ({ isSidebarOpen }) => {
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

  const sendMessage = async (textToSend = input) => {
    const text = textToSend.trim();
    if (!text || loading) return;

    setInput("");
    if (textareaRef.current) textareaRef.current.style.height = "auto";

    const userMessage = { sender: "user", text, time: formatTime() };
    setMessages((prev) => [...prev, userMessage]);
    setLoading(true);

    try {
      const res = await axios.post(
        "http://localhost:8084/api/ai/chat",
        { message: text },
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

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const clearChat = () => setMessages([]);

  return (
    <div className={`doc-container ${isSidebarOpen ? "shifted" : ""}`}>
      <h2 className="doc-title">AI Financial Assistant</h2>

      <div className="doc-box">
        {messages.length === 0 ? (
          <div className="doc-empty">
            <p className="doc-empty-title">Ask anything about your finances</p>
            <div className="doc-suggestions">
              {SUGGESTIONS.map((s) => (
                <button key={s} className="sug-btn" onClick={() => sendMessage(s)}>
                  {s}
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg, i) => (
            <div key={i} className={`doc-message ${msg.sender}`}>
              <div className="msg-content">{msg.text}</div>
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
        <span className="rag-badge">AI Intelligence</span>
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
          placeholder="Type your message..."
          value={input}
          onChange={handleInput}
          onKeyDown={handleKeyPress}
          rows={1}
          disabled={loading}
        />
        <button className="doc-send-btn" onClick={() => sendMessage()} disabled={loading}>
          Send
        </button>
      </div>
    </div>
  );
};

export default AIChat;