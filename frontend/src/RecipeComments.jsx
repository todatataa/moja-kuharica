import { useEffect, useId, useState } from "react";
import { API, apiFetch, errorMessage } from "./api";
import { useAuth } from "./AuthContext";

const MAX_COMMENT_LENGTH = 500;
const loginMessage = "Za komentiranje se moraš prijaviti.";
const loadErrorMessage = "Komentarjev ni bilo mogoče naložiti.";
const dateFormat = new Intl.DateTimeFormat("sl-SI", {
  dateStyle: "medium",
  timeStyle: "short",
});

export default function RecipeComments({ recipeId }) {
  const { user, loading: authLoading, sessionExpired } = useAuth();
  const headingId = useId();
  const url = `${API}/${recipeId}/comments`;
  const [comments, setComments] = useState(null);
  const [loadError, setLoadError] = useState("");
  const [text, setText] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [sending, setSending] = useState(false);

  useEffect(() => {
    const controller = new AbortController();

    async function loadComments() {
      try {
        const response = await apiFetch(url, { signal: controller.signal });
        if (!response.ok) throw new Error(await errorMessage(response, loadErrorMessage));

        const list = await response.json();
        if (!controller.signal.aborted) setComments(list);
      } catch (error) {
        if (!controller.signal.aborted) {
          setLoadError(error instanceof TypeError ? loadErrorMessage : error.message);
        }
      }
    }

    loadComments();
    return () => controller.abort();
  }, [url]);

  async function submit(event) {
    event.preventDefault();
    if (!user || sending) return;
    if (/^\p{White_Space}*$/u.test(text)) {
      setSubmitError("Vnesi besedilo komentarja.");
      return;
    }
    if ([...text].length > MAX_COMMENT_LENGTH) {
      setSubmitError("Komentar lahko vsebuje največ 500 znakov.");
      return;
    }

    setSending(true);
    setSubmitError("");
    const fallback = "Komentarja ni bilo mogoče shraniti.";
    try {
      const response = await apiFetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text }),
      });
      if (response.status === 401) {
        sessionExpired();
        return;
      }
      if (!response.ok) throw new Error(await errorMessage(response, fallback));

      const comment = await response.json();
      setComments((list) => [comment, ...(list ?? [])]);
      setText("");
    } catch (error) {
      setSubmitError(error instanceof TypeError ? fallback : error.message);
    } finally {
      setSending(false);
    }
  }

  const loadingComments = comments === null && !loadError;

  return (
    <section className="recipe-comments" aria-labelledby={headingId}>
      <h4 id={headingId}>Komentarji</h4>
      {user ? (
        <form onSubmit={submit}>
          <label>
            Tvoj komentar (do 500 znakov)
            <textarea
              value={text}
              disabled={sending}
              onChange={(event) => {
                setText(event.target.value);
                setSubmitError("");
              }}
            />
          </label>
          <button className="btn-primary" type="submit" disabled={sending || loadingComments}>
            {sending ? "Pošiljam …" : "Objavi komentar"}
          </button>
          {submitError && <p className="status error" role="alert">{submitError}</p>}
        </form>
      ) : !authLoading && (
        <p className="comment-hint">{loginMessage}</p>
      )}

      {loadingComments && <p className="comment-hint" role="status">Nalagam komentarje …</p>}
      {loadError && <p className="status error" role="alert">{loadError}</p>}
      {comments?.length === 0 && <p className="comment-hint">Še ni komentarjev.</p>}
      {comments?.length > 0 && (
        <ul className="comment-list">
          {comments.map((comment) => (
            <li key={comment.id}>
              <div className="comment-meta">
                <strong>{comment.author}</strong>
                <time dateTime={comment.createdAt}>{dateFormat.format(new Date(comment.createdAt))}</time>
              </div>
              <p>{comment.text}</p>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
