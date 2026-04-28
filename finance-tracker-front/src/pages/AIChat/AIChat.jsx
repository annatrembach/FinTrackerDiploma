import React, { useState } from "react";
import axios from "axios";
import "./AIChat.css";

const AIChat = ({ isSidebarOpen }) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");

  const jwt = localStorage.getItem("jwt");

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMessage = { sender: "user", text: input };
    setMessages((prev) => [...prev, userMessage]);

    try {
      const res = await axios.post(
        "http://localhost:8084/api/ai/chat",
        { message: input },
        {
          headers: {
            Authorization: `Bearer ${jwt}`,
          },
        }
      );

      const aiMessage = { sender: "ai", text: res.data };
      setMessages((prev) => [...prev, aiMessage]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { sender: "ai", text: "Error: AI service unavailable" },
      ]);
    }

    setInput("");
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") sendMessage();
  };

  return (
    <div className={`chat-container ${isSidebarOpen ? "shifted" : ""}`}>
      <h2 className="chat-title">AI Assistant</h2>

      <div className="chat-box">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`chat-message ${
              msg.sender === "user" ? "user" : "ai"
            }`}
          >
            {msg.text}
          </div>
        ))}
      </div>

      <div className="chat-input-area">
        <input
          type="text"
          placeholder="Type your message..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyPress}
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
};

export default AIChat;