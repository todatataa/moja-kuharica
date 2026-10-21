import { useEffect, useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api/recipes";

const emptyForm = {
  title: "",
  description: "",
  ingredients: "",
  instructions: "",
  preparationTimeMinutes: "",
};

export default function App() {
  const [recipes, setRecipes] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [openId, setOpenId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [confirmId, setConfirmId] = useState(null);
  const [status, setStatus] = useState({ text: "", error: false });

  const say = (text, error = false) => setStatus({ text, error });

  const loadAll = async () => {
    try {
      const res = await fetch(API);
      if (!res.ok) throw new Error();
      setRecipes(await res.json());
    } catch {
      say("Zaledje ni dosegljivo. Preveri, ali teče na vratih 8080.", true);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  // CREATE + UPDATE
  const handleSubmit = async (e) => {
    e.preventDefault();
    const body = { ...form, preparationTimeMinutes: Number(form.preparationTimeMinutes) || 0 };
    const url = editingId ? `${API}/${editingId}` : API;
    try {
      const res = await fetch(url, {
        method: editingId ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (!res.ok) throw new Error();
      say(editingId ? "Recept je shranjen." : "Recept je dodan.");
      setForm(emptyForm);
      setEditingId(null);
      setOpenId(null);
      setDetail(null);
      loadAll();
    } catch {
      say("Recepta ni bilo mogoče shraniti.", true);
    }
  };

  // READ (en zapis)
  const toggleDetail = async (id) => {
    if (openId === id) {
      setOpenId(null);
      setDetail(null);
      return;
    }
    try {
      const res = await fetch(`${API}/${id}`);
      if (!res.ok) throw new Error();
      setDetail(await res.json());
      setOpenId(id);
    } catch {
      say("Recepta ni bilo mogoče naložiti.", true);
    }
  };

  const startEdit = (r) => {
    setEditingId(r.id);
    setForm({
      title: r.title ?? "",
      description: r.description ?? "",
      ingredients: r.ingredients ?? "",
      instructions: r.instructions ?? "",
      preparationTimeMinutes: r.preparationTimeMinutes ?? "",
    });
    say("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  // DELETE
  const handleDelete = async (id) => {
    try {
      const res = await fetch(`${API}/${id}`, { method: "DELETE" });
      if (!res.ok) throw new Error();
      say("Recept je izbrisan.");
      if (openId === id) {
        setOpenId(null);
        setDetail(null);
      }
      if (editingId === id) cancelEdit();
      setConfirmId(null);
      loadAll();
    } catch {
      say("Recepta ni bilo mogoče izbrisati.", true);
    }
  };

  const splitList = (text) =>
    (text || "").split(",").map((s) => s.trim()).filter(Boolean);

  return (
    <div className="page">
      <header className="masthead">
        <h1>Moja kuharica</h1>
        <p>Recepti, ki jih ne želiš izgubiti.</p>
      </header>

      <div className="layout">
        <section className="editor">
          {editingId && <span className="editing-badge">Urejanje recepta</span>}
          <h2>{editingId ? "Uredi recept" : "Nov recept"}</h2>
          <p className="hint">Sestavine loči z vejico.</p>
          <form onSubmit={handleSubmit}>
            <label>
              Naslov
              <input name="title" value={form.title} onChange={handleChange} placeholder="npr. Palačinke" required />
            </label>
            <label>
              Kratek opis
              <input name="description" value={form.description} onChange={handleChange} placeholder="npr. Klasične palačinke za zajtrk" />
            </label>
            <label>
              Sestavine
              <textarea name="ingredients" value={form.ingredients} onChange={handleChange} placeholder="200 g moke, 2 jajci, 300 ml mleka" />
            </label>
            <label>
              Navodila
              <textarea name="instructions" value={form.instructions} onChange={handleChange} placeholder="Zmešaj sestavine in speci na ponvi." />
            </label>
            <label>
              Čas priprave (minute)
              <input type="number" min="0" name="preparationTimeMinutes" value={form.preparationTimeMinutes} onChange={handleChange} placeholder="30" />
            </label>
            <div className="actions">
              <button type="submit" className="btn-primary">
                {editingId ? "Shrani spremembe" : "Dodaj recept"}
              </button>
              {editingId && (
                <button type="button" className="btn-text" onClick={cancelEdit}>
                  Prekliči
                </button>
              )}
            </div>
          </form>
          <p className={`status ${status.error ? "error" : ""}`} role="status">{status.text}</p>
        </section>

        <section>
          <div className="list-head">
            <h2>Vsi recepti</h2>
            <span className="count">{recipes.length}</span>
          </div>

          {recipes.length === 0 ? (
            <p className="empty">Še ni receptov. Dodaj prvega v obrazcu.</p>
          ) : (
            <ul className="recipes">
              {recipes.map((r) => (
                <li key={r.id} className="recipe">
                  <div className="recipe-row">
                    <div className="time">
                      <strong>{r.preparationTimeMinutes}</strong>
                      <span>min</span>
                    </div>
                    <div>
                      <h3>{r.title}</h3>
                      {r.description && <p className="desc">{r.description}</p>}
                      <div className="row-actions">
                        <button className="btn-text" onClick={() => toggleDetail(r.id)}>
                          {openId === r.id ? "Skrij" : "Poglej"}
                        </button>
                        <button className="btn-text" onClick={() => startEdit(r)}>Uredi</button>
                        {confirmId === r.id ? (
                          <>
                            <span className="confirm">Izbrišem recept?</span>
                            <button className="btn-text danger" onClick={() => handleDelete(r.id)}>Da, izbriši</button>
                            <button className="btn-text" onClick={() => setConfirmId(null)}>Ne</button>
                          </>
                        ) : (
                          <button className="btn-text danger" onClick={() => setConfirmId(r.id)}>Izbriši</button>
                        )}
                      </div>

                      {openId === r.id && detail && (
                        <div className="detail">
                          <div>
                            <h4>Sestavine</h4>
                            <ul>
                              {splitList(detail.ingredients).map((s, i) => (
                                <li key={i}>{s}</li>
                              ))}
                            </ul>
                          </div>
                          <div>
                            <h4>Navodila</h4>
                            <p>{detail.instructions}</p>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
