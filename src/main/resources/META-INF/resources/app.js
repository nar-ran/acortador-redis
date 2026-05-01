document.addEventListener('DOMContentLoaded', () => {
    const formularioAcortar = document.getElementById('formulario-acortar');
    const urlOriginalInput = document.getElementById('urlOriginal');
    const tiempoVidaInput = document.getElementById('tiempoVida');
    const resultadoDiv = document.getElementById('resultado');
    const enlaceCortoA = document.getElementById('enlaceCorto');
    const btnCopiar = document.getElementById('btnCopiar');
    const btnProbar = document.getElementById('btnProbar');

    const codigoBuscarInput = document.getElementById('codigoBuscar');
    const btnBuscarAnaliticas = document.getElementById('btnBuscarAnaliticas');
    const metricasPanel = document.getElementById('metricas-panel');
    const clicksTotalesSpan = document.getElementById('clicks-totales');

    const listaRanking = document.getElementById('lista-ranking');
    const contenedorStream = document.getElementById('contenedor-stream');
    const toast = document.getElementById('toast');

    let codigoActivo = '';

    // Control de Tema Claro/Oscuro
    const btnTema = document.getElementById('btn-tema');
    const temaGuardado = localStorage.getItem('tema') || 'oscuro';

    if (temaGuardado === 'claro') {
        document.body.classList.add('light-mode');
        btnTema.textContent = 'Modo Oscuro';
    } else {
        btnTema.textContent = 'Modo Claro';
    }

    btnTema.addEventListener('click', () => {
        if (document.body.classList.contains('light-mode')) {
            document.body.classList.remove('light-mode');
            btnTema.textContent = 'Modo Claro';
            localStorage.setItem('tema', 'oscuro');
        } else {
            document.body.classList.add('light-mode');
            btnTema.textContent = 'Modo Oscuro';
            localStorage.setItem('tema', 'claro');
        }
    });

    // 1. Crear enlace acortado
    formularioAcortar.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const urlOriginal = urlOriginalInput.value;
        const tiempoVida = tiempoVidaInput.value ? parseInt(tiempoVidaInput.value) : null;

        try {
            const respuesta = await fetch('/api/enlaces', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ urlOriginal, tiempoVidaSegundos: tiempoVida })
            });

            if (respuesta.status === 429) {
                const err = await respuesta.json();
                mostrarToast(err.mensaje || 'Límite de peticiones superado.');
                return;
            }

            if (!respuesta.ok) {
                const err = await respuesta.json();
                alert('Error: ' + (err.error || 'No se pudo crear el enlace'));
                return;
            }

            const enlace = await respuesta.json();
            
            // Mostrar resultado en la UI
            enlaceCortoA.href = enlace.urlCorta;
            enlaceCortoA.textContent = enlace.urlCorta;
            resultadoDiv.style.display = 'flex';
            
            // Cargar automáticamente en el buscador de analíticas
            codigoBuscarInput.value = enlace.codigo;
            consultarAnaliticas(enlace.codigo);

            // Limpiar formulario
            urlOriginalInput.value = '';
            tiempoVidaInput.value = '';

        } catch (error) {
            console.error('Error al acortar URL:', error);
            alert('Error de conexión con el servidor.');
        }
    });

    // 2. Copiar enlace corto al portapapeles
    btnCopiar.addEventListener('click', () => {
        navigator.clipboard.writeText(enlaceCortoA.textContent);
        const textoOriginal = btnCopiar.textContent;
        btnCopiar.textContent = '¡Copiado!';
        setTimeout(() => btnCopiar.textContent = textoOriginal, 2000);
    });

    // 3. Probar clic (abre en pestaña nueva y dispara el contador)
    btnProbar.addEventListener('click', () => {
        window.open(enlaceCortoA.href, '_blank');
    });

    // 4. Consultar analíticas
    btnBuscarAnaliticas.addEventListener('click', () => {
        const codigo = codigoBuscarInput.value.trim();
        if (codigo) {
            consultarAnaliticas(codigo);
        }
    });

    async function consultarAnaliticas(codigo) {
        try {
            const respuesta = await fetch(`/api/enlaces/${codigo}/analiticas`);
            if (!respuesta.ok) {
                alert('No se encontraron estadísticas para este código.');
                return;
            }
            const data = await respuesta.json();
            codigoActivo = codigo;
            
            clicksTotalesSpan.textContent = data.clicsTotales;
            metricasPanel.style.display = 'block';

            // Actualizar barras de progreso por navegador
            actualizarBarra('chrome', data.clicsPorNavegador.chrome || 0, data.clicsTotales);
            actualizarBarra('firefox', data.clicsPorNavegador.firefox || 0, data.clicsTotales);
            actualizarBarra('safari', data.clicsPorNavegador.safari || 0, data.clicsTotales);
            actualizarBarra('edge', data.clicsPorNavegador.edge || 0, data.clicsTotales);
            actualizarBarra('otros', data.clicsPorNavegador.otros || 0, data.clicsTotales);

        } catch (error) {
            console.error('Error al consultar analíticas:', error);
        }
    }

    function actualizarBarra(id, clics, totales) {
        const porc = totales > 0 ? Math.round((clics / totales) * 100) : 0;
        document.getElementById(`porc-${id}`).textContent = `${porc}% (${clics} clics)`;
        document.getElementById(`relleno-${id}`).style.width = `${porc}%`;
    }

    // 5. Cargar ranking de popularidad (ZSet)
    async function cargarRanking() {
        try {
            const respuesta = await fetch('/api/enlaces/ranking?limite=5');
            const ranking = await respuesta.json();
            
            listaRanking.innerHTML = '';
            const entradas = Object.entries(ranking);

            if (entradas.length === 0) {
                listaRanking.innerHTML = '<li style="color: var(--text-secondary); text-align: center; padding: 1rem;">No hay clics registrados aún.</li>';
                return;
            }

            entradas.forEach(([codigo, clics], indice) => {
                const li = document.createElement('li');
                li.className = 'item-ranking';
                li.innerHTML = `
                    <div>
                        <span style="font-weight:600; color:var(--text-secondary); margin-right:0.5rem;">#${indice + 1}</span>
                        <span class="ranking-codigo">${codigo}</span>
                        <a href="http://localhost:8080/${codigo}" target="_blank" style="color: #a78bfa; text-decoration:none; font-size:0.85rem; margin-left:0.5rem;">Ir</a>
                    </div>
                    <span class="ranking-clicks">${clics} clics</span>
                `;
                listaRanking.appendChild(li);
            });
        } catch (error) {
            console.error('Error al cargar ranking:', error);
        }
    }

    // 6. Escuchar eventos en vivo SSE (Pub/Sub)
    function conectarSse() {
        const source = new EventSource('/api/live/clicks');
        
        // Limpiamos el texto por defecto la primera vez que recibimos algo
        let primerEvento = true;

        source.onmessage = (event) => {
            if (primerEvento) {
                contenedorStream.innerHTML = '';
                primerEvento = false;
            }

            const clickInfo = JSON.parse(event.data);
            
            // Crear elemento visual del evento
            const div = document.createElement('div');
            div.className = 'click-evento';
            
            const hora = new Date(clickInfo.fecha).toLocaleTimeString();
            
            div.innerHTML = `
                <div class="click-evento-fecha">${hora} - Clic registrado</div>
                Enlace acortado <span class="click-evento-codigo">${clickInfo.codigo}</span> 
                visitado desde <strong>${clickInfo.navegador}</strong>.
            `;
            
            // Insertar al inicio de la lista
            contenedorStream.insertBefore(div, contenedorStream.firstChild);
            
            // Mantener solo los últimos 15 clics para rendimiento
            if (contenedorStream.children.length > 15) {
                contenedorStream.removeChild(contenedorStream.lastChild);
            }

            // Actualizar ranking global
            cargarRanking();

            // Si el clic pertenece al código que estamos analizando actualmente, refrescar la gráfica
            if (clickInfo.codigo === codigoActivo) {
                consultarAnaliticas(codigoActivo);
            }
        };

        source.onerror = (err) => {
            console.error('Error en conexión SSE, reintentando...', err);
            source.close();
            setTimeout(conectarSse, 5000); // Reconexión automática
        };
    }

    // Mostrar alerta flotante ante bloqueos del Rate Limiter
    function mostrarToast(mensaje) {
        toast.textContent = mensaje;
        toast.style.display = 'block';
        setTimeout(() => {
            toast.style.display = 'none';
        }, 5000);
    }

    // Inicialización del Dashboard
    cargarRanking();
    conectarSse();
});
