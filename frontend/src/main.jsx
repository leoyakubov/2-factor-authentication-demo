import React from "react";
import * as ReactDOMClient from "react-dom/client";
import "./index.css";
import App from "./App";

const { createRoot } = ReactDOMClient;
const root = createRoot(document.getElementById("root"));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
