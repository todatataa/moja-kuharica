import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { AUTH_API, apiFetch, errorMessage } from "./api";

const AuthContext = createContext(null);

const JSON_HEADERS = { "Content-Type": "application/json" };
const UNREACHABLE = "Zaledje ni dosegljivo. Preveri, ali teče na vratih 8080.";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Ob zagonu vprašamo zaledje, ali je seja še veljavna (po osvežitvi strani).
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await apiFetch(`${AUTH_API}/me`);
        if (!cancelled) setUser(res.ok ? await res.json() : null);
      } catch {
        if (!cancelled) setUser(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  // Funkcije ob napaki vržejo Error s sporočilom, ki ga prikaže obrazec.
  const login = useCallback(async (username, password) => {
    let res;
    try {
      res = await apiFetch(`${AUTH_API}/login`, {
        method: "POST",
        headers: JSON_HEADERS,
        body: JSON.stringify({ username, password }),
      });
    } catch {
      throw new Error(UNREACHABLE);
    }
    if (!res.ok) throw new Error(await errorMessage(res, "Prijava ni uspela."));
    setUser(await res.json());
  }, []);

  const register = useCallback(
    async (username, email, password) => {
      let res;
      try {
        res = await apiFetch(`${AUTH_API}/register`, {
          method: "POST",
          headers: JSON_HEADERS,
          body: JSON.stringify({ username, email, password }),
        });
      } catch {
        throw new Error(UNREACHABLE);
      }
      if (!res.ok) throw new Error(await errorMessage(res, "Registracija ni uspela."));
      // Po uspešni registraciji uporabnika takoj prijavimo.
      await login(username, password);
    },
    [login],
  );

  const logout = useCallback(async () => {
    try {
      await apiFetch(`${AUTH_API}/logout`, { method: "POST" });
    } catch {
      // tudi če zaledje ni dosegljivo, uporabnika v obličju odjavimo
    }
    setUser(null);
  }, []);

  // Za primer, ko zaledje vrne 401, ker je seja potekla.
  const sessionExpired = useCallback(() => setUser(null), []);

  const value = useMemo(
    () => ({ user, loading, login, register, logout, sessionExpired }),
    [user, loading, login, register, logout, sessionExpired],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth mora biti uporabljen znotraj AuthProvider.");
  return ctx;
}
