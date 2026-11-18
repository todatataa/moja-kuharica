import { useState } from "react";
import { useAuth } from "./AuthContext";

const emptyFields = { username: "", email: "", password: "" };
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Enaka pravila kot v zaledju (RegistrationService), da uporabnik napako vidi takoj.
function validate(mode, f) {
  const username = f.username.trim();
  if (mode === "login") {
    if (!username || !f.password) return "Vnesi uporabniško ime in geslo.";
    return "";
  }
  if (username.length < 3 || username.length > 50) return "Uporabniško ime mora imeti od 3 do 50 znakov.";
  if (!EMAIL_PATTERN.test(f.email.trim())) return "Elektronski naslov ni veljaven.";
  if (f.password.length < 8) return "Geslo mora imeti vsaj 8 znakov.";
  return "";
}

export default function AuthPanel({ notice }) {
  const { login, register } = useAuth();
  const [mode, setMode] = useState("login");
  const [fields, setFields] = useState(emptyFields);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const isRegister = mode === "register";

  const handleChange = (e) => setFields({ ...fields, [e.target.name]: e.target.value });

  const switchMode = () => {
    setMode(isRegister ? "login" : "register");
    setFields({ ...fields, password: "" });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const problem = validate(mode, fields);
    if (problem) {
      setError(problem);
      return;
    }
    setBusy(true);
    setError("");
    try {
      if (isRegister) {
        await register(fields.username.trim(), fields.email.trim(), fields.password);
      } else {
        await login(fields.username.trim(), fields.password);
      }
      // ob uspehu se prikaže uporabniški vmesnik za prijavljene, zato tu ni več kaj nastavljati
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  const message = error || (notice && notice.text) || "";
  const isError = Boolean(error) || Boolean(notice && notice.error);

  return (
    <section className="editor">
      <h2>{isRegister ? "Registracija" : "Prijava"}</h2>
      <p className="hint">
        Recepte lahko pregleduješ brez prijave. Za dodajanje, urejanje in brisanje se prijavi.
      </p>
      <form onSubmit={handleSubmit} noValidate>
        <label>
          {isRegister ? "Uporabniško ime" : "Uporabniško ime ali e-naslov"}
          <input
            name="username"
            value={fields.username}
            onChange={handleChange}
            autoComplete="username"
            placeholder={isRegister ? "npr. ana" : "ana ali ana@primer.si"}
          />
        </label>
        {isRegister && (
          <label>
            E-naslov
            <input
              type="email"
              name="email"
              value={fields.email}
              onChange={handleChange}
              autoComplete="email"
              placeholder="ana@primer.si"
            />
          </label>
        )}
        <label>
          Geslo
          <input
            type="password"
            name="password"
            value={fields.password}
            onChange={handleChange}
            autoComplete={isRegister ? "new-password" : "current-password"}
            placeholder={isRegister ? "vsaj 8 znakov" : ""}
          />
        </label>
        <div className="actions">
          <button type="submit" className="btn-primary" disabled={busy}>
            {busy ? "Počakaj ..." : isRegister ? "Registriraj se" : "Prijavi se"}
          </button>
        </div>
      </form>
      <p className="auth-switch">
        {isRegister ? "Že imaš račun?" : "Nimaš računa?"}{" "}
        <button type="button" className="btn-text" onClick={switchMode}>
          {isRegister ? "Prijava" : "Registracija"}
        </button>
      </p>
      <p className={`status ${isError ? "error" : ""}`} role="status">{message}</p>
    </section>
  );
}
