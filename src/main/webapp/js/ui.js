export function crearElemento(etiqueta, { texto, clase, atributos = {}, hijos = [] } = {}) {
    const elemento = document.createElement(etiqueta);
    if (texto !== undefined && texto !== null) {
        elemento.textContent = String(texto);
    }
    if (clase) {
        elemento.className = clase;
    }
    for (const [nombre, valor] of Object.entries(atributos)) {
        if (valor !== undefined && valor !== null) {
            elemento.setAttribute(nombre, String(valor));
        }
    }
    elemento.append(...hijos);
    return elemento;
}

export function vaciar(elemento) {
    elemento.replaceChildren();
}

export function estado(elemento, mensaje = "", { error = false } = {}) {
    elemento.textContent = mensaje;
    elemento.classList.toggle("is-error", error);
}

export function cargar(elemento, mensaje, ocupado) {
    elemento.setAttribute("aria-busy", String(ocupado));
    estado(elemento, mensaje);
}

export function limpiarErrores(formulario) {
    formulario.querySelectorAll("[data-error-for]").forEach((nodo) => {
        nodo.textContent = "";
    });
}

export function erroresDeCampo(formulario, detalles = {}) {
    limpiarErrores(formulario);
    let mostrados = 0;
    formulario.querySelectorAll("[data-error-for]").forEach((nodo) => {
        const mensaje = detalles[nodo.dataset.errorFor];
        if (mensaje) {
            nodo.textContent = mensaje;
            mostrados += 1;
        }
    });
    return mostrados;
}

export function notificar(mensaje, tipo = "success") {
    const region = document.getElementById("toast-region");
    if (!region) {
        return;
    }
    const toast = crearElemento("div", {
        texto: mensaje,
        clase: `toast toast-${tipo}`,
        atributos: { role: tipo === "error" ? "alert" : "status" }
    });
    region.append(toast);
    window.setTimeout(() => toast.remove(), 5000);
}

export function mostrarError(error, formulario) {
    if (formulario && error.status === 400) {
        erroresDeCampo(formulario, error.detalles);
    }
    notificar(error.message || "Ha ocurrido un error inesperado.", "error");
}

export function confirmar(mensaje) {
    const dialogo = document.getElementById("confirm-dialog");
    if (!dialogo) {
        return Promise.resolve(false);
    }
    dialogo.querySelector("[data-confirm-message]").textContent = mensaje;
    dialogo.returnValue = "";
    return new Promise((resolver) => {
        dialogo.addEventListener("close", () => resolver(dialogo.returnValue === "confirm"), { once: true });
        dialogo.showModal();
    });
}

export function boton(texto, clase, accion) {
    const control = crearElemento("button", { texto, clase, atributos: { type: "button" } });
    control.addEventListener("click", accion);
    return control;
}

export function formatoNumero(valor) {
    return new Intl.NumberFormat("es-ES", { maximumFractionDigits: 2 }).format(Number(valor ?? 0));
}

export function formatoMoneda(valor) {
    return new Intl.NumberFormat("es-ES", { style: "currency", currency: "EUR" }).format(Number(valor ?? 0));
}

export function formatoFecha(valor) {
    if (!valor) {
        return "—";
    }
    const fecha = new Date(valor);
    if (Number.isNaN(fecha.getTime())) {
        return String(valor).replace("T", " ");
    }
    return new Intl.DateTimeFormat("es-ES", { dateStyle: "short", timeStyle: "short" }).format(fecha);
}

export function prepararDialogos() {
    document.querySelectorAll("[data-close-dialog]").forEach((control) => {
        control.addEventListener("click", () => {
            document.getElementById(control.dataset.closeDialog)?.close();
        });
    });
}