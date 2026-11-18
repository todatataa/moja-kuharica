export const API = import.meta.env.VITE_API_URL || "http://localhost:8080/api/recipes";
export const AUTH_API = import.meta.env.VITE_AUTH_URL || "http://localhost:8080/api/auth";

export const FAVORITES_API =
    import.meta.env.VITE_FAVORITES_URL ||
    "http://localhost:8080/api/me/favorites";


// Piškotek seje (JSESSIONID) brskalnik pošlje samo, če je nastavljen credentials: "include".
export function apiFetch(url, options = {}) {
  return fetch(url, { credentials: "include", ...options });
}

// Zaledje pošlje razlago napake v polju "message"; če je ni, uporabimo rezervno besedilo.
export async function errorMessage(res, fallback) {
  try {
    const data = await res.json();
    if (data && data.message) return data.message;
  } catch {
    // odgovor ni JSON
  }
  return fallback;
}
