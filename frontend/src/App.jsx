import { useEffect, useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api/recipes";
const PAGE_SIZE = 5;

const SORT_OPTIONS = [
  { value: "id,desc", label: "Najnovejši" },
  { value: "id,asc", label: "Najstarejši" },
  { value: "title,asc", label: "Naslov A–Z" },
  { value: "title,desc", label: "Naslov Z–A" },
  { value: "preparationTimeMinutes,asc", label: "Najhitrejši" },
  { value: "preparationTimeMinutes,desc", label: "Najdaljši čas" },
];

const emptyForm = {
  title: "",
  description: "",
  ingredients: "",
  instructions: "",
  preparationTimeMinutes: "",
};

export default function App() {
  const [recipes, setRecipes] = useState([]);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [sort, setSort] = useState("id,desc");
  const [refresh, setRefresh] = useState(0);

  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [openId, setOpenId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [confirmId, setConfirmId] = useState(null);
  const [status, setStatus] = useState({ text: "", error: false });

  const say = (text, error = false) => setStatus({ text, error });
  const reload = () => setRefresh((n) => n + 1);

  // Iskanje: počakaj 300 ms po zadnji črki, nato išči od prve strani
  useEffect(() => {
    const t = setTimeout(() => {
      setSearch(searchInput.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(t);
  }, [searchInput]);

  // READ (vsi): vedno ko se spremeni iskanje, stran, razvrščanje ali podatki
  useEffect(() => {
    let cancelled = false;
    const [sortBy, direction] = sort.split(",");
    const params = new URLSearchParams({ q: search, page, size: PAGE_SIZE, sortBy, direction });

    (async () => {
      try {
        const res = await fetch(`${API}?${params}`);
        if (!res.ok) throw new Error();
        const data = await res.json();
        if (cancelled) return;
        if (data.content.length === 0 && page > 0) {
          setPage(page - 1); // izbrisan zadnji recept na strani
          return;
        }
        setRecipes(data.content);
        setTotal(data.totalElements);
        setTotalPages(data.totalPages);
        setStatus((s) => (s.error ? { text: "", error: false } : s));
      } catch {
        if (!cancelled) say("Zaledje ni dosegljivo. Preveri, ali teče na vratih 8080.", true);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [search, page, sort, refresh]);

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
      if (!editingId) {
        // nov recept: počisti iskanje in pojdi na prvo stran
        setSearchInput("");
        setSearch("");
        setPage(0);
      }
      say(editingId ? "Recept je shranjen." : "Recept je dodan.");
      setForm(emptyForm);
      setEditingId(null);
      setOpenId(null);
      setDetail(null);
      reload();
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
      reload();
    } catch {
      say("Recepta ni bilo mogoče izbrisati.", true);
    }
  };

  const splitList = (text) => (text || "").split(",").map((s) => s.trim()).filter(Boolean);

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
            <span className="count">
              {search ? `${total} zadetkov za »${search}«` : `${total} receptov`}
            </span>
          </div>

          <div className="toolbar">
            <input
              type="search"
              className="search"
              placeholder="Išči po naslovu ali sestavinah"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              aria-label="Iskanje receptov"
            />
            <select
              className="sort"
              value={sort}
              onChange={(e) => {
                setSort(e.target.value);
                setPage(0);
              }}
              aria-label="Razvrščanje"
            >
              {SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          {recipes.length === 0 ? (
            <p className="empty">
              {search ? "Ni zadetkov. Poskusi z drugo besedo." : "Še ni receptov. Dodaj prvega v obrazcu."}
            </p>
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

          {totalPages > 1 && (
            <nav className="pager" aria-label="Strani">
              <button className="btn-text" disabled={page === 0} onClick={() => setPage(page - 1)}>
                Prejšnja
              </button>
              <span>Stran {page + 1} od {totalPages}</span>
              <button className="btn-text" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>
                Naslednja
              </button>
            </nav>
          )}
        </section>
      </div>
    </div>
  );
}
