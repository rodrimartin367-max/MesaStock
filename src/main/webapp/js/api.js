const BASE_URL = "/api";

async function solicitar(ruta, opciones = {}) {
    let respuesta;
    try {
        respuesta = await fetch(`${BASE_URL}${ruta}`, {
            ...opciones,
            headers: {
                Accept: "application/json",
                ...(opciones.body ? { "Content-Type": "application/json" } : {}),
                ...opciones.headers
            }
        });
    } catch (causa) {
        const error = new Error("No se pudo conectar con el servidor. Comprueba la conexión e inténtalo de nuevo.");
        error.cause = causa;
        error.status = 0;
        error.detalles = {};
        throw error;
    }

    if (respuesta.status === 204) {
        return null;
    }

    let contenido;
    try {
        contenido = await respuesta.json();
    } catch {
        contenido = {};
    }

    if (!respuesta.ok) {
        const error = new Error(contenido.mensaje || `La solicitud ha fallado (${respuesta.status}).`);
        error.status = respuesta.status;
        error.detalles = contenido.detalles || {};
        throw error;
    }
    return contenido;
}

export const api = {
    get: (ruta) => solicitar(ruta),
    post: (ruta, datos) => solicitar(ruta, { method: "POST", body: JSON.stringify(datos) }),
    put: (ruta, datos) => solicitar(ruta, { method: "PUT", body: JSON.stringify(datos) }),
    delete: (ruta) => solicitar(ruta, { method: "DELETE" })
};