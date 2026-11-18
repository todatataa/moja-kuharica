import { useState } from "react";
import { API, apiFetch, errorMessage } from "./api";

const stars = [1, 2, 3, 4, 5];
const decimal = new Intl.NumberFormat("sl-SI", {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});
const loginMessage = "Za ocenjevanje se moraš prijaviti.";

export function RatingSummary({ recipe }) {
  const { averageRating, ratingCount } = recipe;
  const label = ratingCount
    ? `${decimal.format(averageRating)} / 5 · Število ocen: ${ratingCount}`
    : "Še brez ocen";

  return (
    <p className="rating-summary">
      <span aria-hidden="true">★</span> {label}
    </p>
  );
}

export default function RecipeRating({ recipe, user, onRated }) {
  const [value, setValue] = useState(0);
  const [busy, setBusy] = useState(false);
  const [status, setStatus] = useState(null);
  const isOwner = Boolean(user) && user.id === recipe.ownerId;

  async function submit(event) {
    event.preventDefault();
    if (!user) {
      setStatus({ message: loginMessage, error: true });
      return;
    }
    if (!stars.includes(value)) {
      setStatus({ message: "Izberi oceno med 1 in 5.", error: true });
      return;
    }

    setBusy(true);
    setStatus(null);
    const fallback = "Ocene ni bilo mogoče shraniti.";
    try {
      const response = await apiFetch(`${API}/${recipe.id}/rating`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ value }),
      });
      if (response.status === 401) throw new Error(loginMessage);
      if (!response.ok) throw new Error(await errorMessage(response, fallback));

      onRated(await response.json());
      setStatus({ message: "Tvoja ocena je shranjena.", error: false });
    } catch (error) {
      setStatus({ message: error instanceof TypeError ? fallback : error.message, error: true });
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="recipe-rating" aria-label="Ocenjevanje recepta">
      <RatingSummary recipe={recipe} />
      {isOwner ? (
        <p className="rating-hint">Svojega recepta ne moreš oceniti.</p>
      ) : (
        <form onSubmit={submit}>
          <fieldset disabled={busy}>
            <legend>Oceni recept</legend>
            <div className="rating-stars">
              {stars.map((star) => (
                <label key={star}>
                  <input
                    type="radio"
                    name="rating"
                    value={star}
                    checked={value === star}
                    onChange={() => { setValue(star); setStatus(null); }}
                    aria-label={`Ocena ${star} od 5`}
                  />
                  <span aria-hidden="true">{star <= value ? "★" : "☆"}</span>
                </label>
              ))}
            </div>
          </fieldset>
          <button type="submit" className="btn-primary" disabled={busy}>
            {busy ? "Shranjujem …" : "Oddaj oceno"}
          </button>
        </form>
      )}
      {status && (
        <p className={`status${status.error ? " error" : ""}`} role="status">{status.message}</p>
      )}
    </section>
  );
}
