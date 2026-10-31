/* Shared design scripts.
   - Screens: ?state=loading | loading-discord | error sets data-state on <html> so a screen can show that state.
   - Toast: the close button and the optional data-duration (ms) auto-hide, see setupToast.
   - Overview (index.html): screen list, device presets, portrait/landscape and state buttons for the live preview. */

(function () {
  var params = new URLSearchParams(location.search);
  var state = params.get('state');
  if (state) document.documentElement.dataset.state = state;

  // Toast: closes with the X button and, when data-duration (ms) is set, hides itself after that time.
  // The countdown pauses while the pointer is over the toast and continues with the time left.
  function setupToast(toast) {
    var region = toast.closest('.toast-region');
    var left = Number(toast.dataset.duration) || 0;
    var startedAt = 0;
    var timer;

    function hide() {
      clearTimeout(timer);
      toast.classList.add('is-leaving');
      // Same length as the toast-out animation in style.css
      setTimeout(function () { region.style.display = 'none'; }, 180);
    }

    function start() {
      if (!left || toast.classList.contains('is-leaving')) return;
      startedAt = Date.now();
      timer = setTimeout(hide, left);
    }

    function pause() {
      if (!left) return;
      clearTimeout(timer);
      left -= Date.now() - startedAt;
    }

    toast.querySelector('.toast__close').addEventListener('click', hide);
    toast.addEventListener('mouseenter', pause);
    toast.addEventListener('mouseleave', start);
    start();
  }

  window.setupToast = setupToast;
  document.querySelectorAll('.toast').forEach(function (toast) {
    // Toasts listed as examples on the Toast page are static: their close buttons do nothing.
    if (!toast.closest('.specimens')) setupToast(toast);
  });

  var frame = document.getElementById('frame');
  if (!frame) return;

  var stage = document.getElementById('stage');
  var wrap = document.getElementById('frameWrap');
  var sizeLabel = document.getElementById('sizeLabel');
  // `short` and `long` are the two sides of the selected device; orientation decides which one is the width.
  var current = { src: null, state: '', short: 427, long: 952, orient: 'portrait', w: 427, h: 952 };

  function applyOrientation() {
    var portrait = current.orient === 'portrait';
    current.w = portrait ? current.short : current.long;
    current.h = portrait ? current.long : current.short;
    activate('.orient', document.querySelector('.orient[data-orient="' + current.orient + '"]'));
  }

  function fit() {
    var pad = 32;
    var scale = Math.min(1, (stage.clientWidth - pad) / current.w, (stage.clientHeight - pad) / current.h);
    wrap.style.width = current.w * scale + 'px';
    wrap.style.height = current.h * scale + 'px';
    frame.style.width = current.w + 'px';
    frame.style.height = current.h + 'px';
    frame.style.transform = 'scale(' + scale + ')';
    sizeLabel.textContent = current.w + ' × ' + current.h + (scale < 1 ? ' · ' + Math.round(scale * 100) + '%' : '');
  }

  function load() {
    if (!current.src) return;
    frame.src = current.src + (current.state ? '?state=' + current.state : '');
  }

  function activate(group, el) {
    document.querySelectorAll(group).forEach(function (node) {
      node.setAttribute('aria-pressed', node === el ? 'true' : 'false');
      node.classList.toggle('is-active', node === el);
    });
  }

  document.querySelectorAll('.screen').forEach(function (el) {
    el.addEventListener('click', function () {
      current.src = el.dataset.src;
      activate('.screen', el);
      load();
    });
  });

  // Mobile / Tablet / Desktop dropdowns: picking a size in one resets the other two.
  // The selected orientation is kept when switching device, so only the Portrait / Landscape chips change it.
  var sizeSelects = document.querySelectorAll('.preset-select');
  sizeSelects.forEach(function (select) {
    select.addEventListener('change', function () {
      var size = select.value.split('x').map(Number);
      current.short = Math.min(size[0], size[1]);
      current.long = Math.max(size[0], size[1]);
      sizeSelects.forEach(function (other) {
        if (other !== select) other.selectedIndex = 0;
        other.classList.toggle('is-active', other === select);
      });
      applyOrientation();
      fit();
    });
  });

  document.querySelectorAll('.orient').forEach(function (el) {
    el.addEventListener('click', function () {
      current.orient = el.dataset.orient;
      applyOrientation();
      fit();
    });
  });

  document.querySelectorAll('.state').forEach(function (el) {
    el.addEventListener('click', function () {
      current.state = el.dataset.state;
      activate('.state', el);
      load();
    });
  });

  window.addEventListener('resize', fit);

  document.querySelector('.screen').click();
  sizeSelects[0].value = '427x952';
  sizeSelects[0].dispatchEvent(new Event('change'));
})();
