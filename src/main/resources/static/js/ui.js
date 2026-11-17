/* Rede de Prato — accessible dialog helper (shared by the pages). */
(function () {
  'use strict';
  let backdrop = null;
  let lastFocus = null;
  let onCloseCb = null;

  function onKey(e) {
    if (e.key === 'Escape') close();
    if (e.key === 'Tab' && backdrop) {
      const f = backdrop.querySelectorAll('button:not([disabled]), a[href], input, select, [tabindex="0"]');
      if (!f.length) return;
      const first = f[0];
      const last = f[f.length - 1];
      if (e.shiftKey && document.activeElement === first) { last.focus(); e.preventDefault(); }
      else if (!e.shiftKey && document.activeElement === last) { first.focus(); e.preventDefault(); }
    }
  }

  /** Opens a dialog with the given inner HTML; returns the .modal element. */
  function open(html, opts) {
    close(true);
    lastFocus = document.activeElement;
    onCloseCb = (opts && opts.onClose) || null;
    backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';
    backdrop.innerHTML = '<div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">' +
      '<button type="button" class="close" aria-label="Fechar">&times;</button>' + html + '</div>';
    document.body.appendChild(backdrop);
    backdrop.querySelector('.close').addEventListener('click', () => close());
    backdrop.addEventListener('click', (e) => { if (e.target === backdrop) close(); });
    document.addEventListener('keydown', onKey);
    const modal = backdrop.querySelector('.modal');
    const focusable = modal.querySelector('h2, [autofocus]') || modal.querySelector('.close');
    if (focusable.tagName === 'H2') focusable.setAttribute('tabindex', '-1');
    focusable.focus();
    return modal;
  }

  function close(silent) {
    if (!backdrop) return;
    backdrop.remove();
    backdrop = null;
    document.removeEventListener('keydown', onKey);
    if (lastFocus && lastFocus.focus) lastFocus.focus();
    const cb = onCloseCb;
    onCloseCb = null;
    if (!silent && cb) cb();
  }

  /** Replaces the icon placeholders <span class="icon-slot" data-icon="x"> with SVG icons. */
  function icons(root) {
    (root || document).querySelectorAll('.icon-slot').forEach((el) => { el.outerHTML = RP.icon(el.dataset.icon); });
  }

  RP.modal = { open, close };
  RP.icons = icons;
})();
