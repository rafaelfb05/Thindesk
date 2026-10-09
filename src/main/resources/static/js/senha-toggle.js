document.addEventListener('click', function (evento) {
    const botao = evento.target.closest('[data-toggle-senha]');
    if (!botao) {
        return;
    }
    const campo = botao.closest('.input-group').querySelector('input');
    const mostrar = campo.type === 'password';

    campo.type = mostrar ? 'text' : 'password';
    botao.setAttribute('aria-label', mostrar ? 'Ocultar senha' : 'Mostrar senha');
    botao.setAttribute('title', mostrar ? 'Ocultar senha' : 'Mostrar senha');

    const icone = botao.querySelector('i');
    if (icone) {
        icone.classList.toggle('bi-eye', !mostrar);
        icone.classList.toggle('bi-eye-slash', mostrar);
    }
});