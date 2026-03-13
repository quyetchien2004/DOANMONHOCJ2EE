(function () {
  const form = document.getElementById('searchForm');
  const rentalModeEl = document.getElementById('rentalMode');
  const hourlyFields = document.getElementById('hourlyFields');
  const dailyFields = document.getElementById('dailyFields');
  const useLocationBtn = document.getElementById('useLocationBtn');
  const resetSearchBtn = document.getElementById('resetSearchBtn');
  const searchBtn = document.getElementById('searchBtn');
  const statusBar = document.getElementById('statusBar');
  const branchResults = document.getElementById('branchResults');
  const bookingAlert = document.getElementById('bookingAlert');
  const mapElement = document.getElementById('branchMap');
  const resultCountEl = document.getElementById('resultCount');
  const roomCountEl = document.getElementById('roomCount');
  const bestPriceEl = document.getElementById('bestPrice');
  const selectedRoomIdEl = document.getElementById('selectedRoomId');
  const selectedRoomLabelEl = document.getElementById('selectedRoomLabel');
  const customerFullNameEl = document.getElementById('customerFullName');
  const confirmBookingBtn = document.getElementById('confirmBookingBtn');
  const roomContextById = {};

  const map = window.L ? L.map(mapElement).setView([16.5, 107.5], 6) : null;
  const bookingModal = window.bootstrap ? new bootstrap.Modal(document.getElementById('bookingModal')) : null;
  let markers = [];

  if (map) {
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);
  }

  let lastSearchPayload = null;

  function formatCurrency(vnd) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(vnd);
  }

  function setStatus(msg, type) {
    statusBar.className = 'status-bar';
    if (type) {
      statusBar.classList.add(type);
    }
    statusBar.textContent = msg;
  }

  function setSummary(branchCount, roomCount, minPrice) {
    if (resultCountEl) {
      resultCountEl.textContent = String(branchCount);
    }
    if (roomCountEl) {
      roomCountEl.textContent = String(roomCount);
    }
    if (bestPriceEl) {
      bestPriceEl.textContent = minPrice == null ? '-' : formatCurrency(minPrice);
    }
  }

  function toggleModeFields() {
    const mode = rentalModeEl.value;
    hourlyFields.style.display = mode === 'HOURLY' ? 'flex' : 'none';
    dailyFields.style.display = mode === 'DAILY' ? 'flex' : 'none';
  }

  function collectPayload() {
    const payload = {
      province: document.getElementById('province').value.trim(),
      rentalMode: rentalModeEl.value,
      maxPrice: document.getElementById('maxPrice').value.trim(),
      voucherCode: document.getElementById('voucherCode').value.trim(),
      userLatitude: document.getElementById('userLatitude').value.trim(),
      userLongitude: document.getElementById('userLongitude').value.trim()
    };

    if (payload.rentalMode === 'HOURLY') {
      payload.startDateTime = document.getElementById('startDateTime').value;
      payload.endDateTime = document.getElementById('endDateTime').value;
    } else {
      payload.startDate = document.getElementById('startDate').value;
      payload.endDate = document.getElementById('endDate').value;
    }

    Object.keys(payload).forEach((k) => {
      if (payload[k] === '') {
        delete payload[k];
      }
    });

    return payload;
  }

  function toQueryString(payload) {
    const qs = new URLSearchParams();
    Object.entries(payload).forEach(([k, v]) => qs.append(k, v));
    return qs.toString();
  }

  function hasRequiredSearchFields(payload) {
    if (!payload || !payload.rentalMode) {
      return false;
    }
    if (payload.rentalMode === 'HOURLY') {
      return Boolean(payload.startDateTime && payload.endDateTime);
    }
    return Boolean(payload.startDate && payload.endDate);
  }

  function roomTypeText(roomType) {
    switch (roomType) {
      case 'SINGLE': return 'Phong don';
      case 'DOUBLE': return 'Phong doi';
      case 'TRIPLE': return 'Phong 3 nguoi';
      case 'FAMILY': return 'Phong gia dinh';
      default: return roomType;
    }
  }

  function renderResults(data) {
    Object.keys(roomContextById).forEach((k) => delete roomContextById[k]);

    if (!Array.isArray(data) || data.length === 0) {
      if (map) {
        markers.forEach((m) => map.removeLayer(m));
        markers = [];
      }
      branchResults.innerHTML = '<div class="note-card">Khong tim thay phong phu hop voi bo loc hien tai.</div>';
      setSummary(0, 0, null);
      return;
    }

    renderMapMarkers(data);
    let roomCount = 0;
    let minPrice = null;

    branchResults.innerHTML = data.map((branch) => {
      const distance = branch.distanceKm == null ? 'Chua co vi tri nguoi dung' : branch.distanceKm + ' km';
      const roomsHtml = (branch.availableRooms || []).map((room) => {
        roomCount += 1;
        if (room.estimatedPrice != null && (minPrice == null || room.estimatedPrice < minPrice)) {
          minPrice = room.estimatedPrice;
        }
        roomContextById[String(room.roomId)] = {
          roomNumber: room.roomNumber,
          floorNumber: room.floorNumber,
          branchName: branch.branchName
        };
        return '<div class="room-item">'
          + '<div class="d-flex justify-content-between align-items-start flex-wrap">'
          + '<div>'
          + '<h4>Phong ' + room.roomNumber + ' - Tang ' + room.floorNumber + '</h4>'
          + '<div class="room-meta">' + roomTypeText(room.roomType) + ' • ' + room.capacity + ' khach</div>'
          + '</div>'
          + '<div>'
          + '<span class="price-chip">Tam tinh: ' + formatCurrency(room.estimatedPrice) + '</span>'
          + (room.hasNiceView ? '<span class="badge-view">View dep</span>' : '')
          + '</div>'
          + '</div>'
          + '<div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">'
          + '<small>Gia gio: ' + formatCurrency(room.hourlyRate) + ' | Gia ngay: ' + formatCurrency(room.dailyRate) + '</small>'
            + '<button type="button" class="btn btn-brand btn-sm js-book-room" data-room-id="' + room.roomId + '">Dat phong nay</button>'
          + '</div>'
          + '</div>';
      }).join('');

      return '<div class="branch-card">'
        + '<div class="branch-head">'
        + '<h3 class="branch-title">' + branch.branchName + ' (' + branch.province + ')</h3>'
        + '<p class="branch-sub">' + branch.address + ' • Khoang cach: ' + distance + '</p>'
        + '<p class="branch-sub">' + branch.totalFloors + ' tang • ' + branch.roomsPerFloor + ' phong/tang</p>'
        + '</div>'
        + '<div class="room-list">' + roomsHtml + '</div>'
        + '</div>';
    }).join('');

      setSummary(data.length, roomCount, minPrice);
  }

  function renderMapMarkers(data) {
    if (!map) {
      return;
    }

    markers.forEach((m) => map.removeLayer(m));
    markers = [];

    const points = [];
    data.forEach((branch) => {
      if (branch.latitude == null || branch.longitude == null) {
        return;
      }
      const marker = L.marker([branch.latitude, branch.longitude]).addTo(map);
      marker.bindPopup('<strong>' + branch.branchName + '</strong><br/>' + branch.address + '<br/>' + branch.province);
      markers.push(marker);
      points.push([branch.latitude, branch.longitude]);
    });

    if (points.length > 0) {
      const bounds = L.latLngBounds(points);
      map.fitBounds(bounds.pad(0.2));
    }
  }

  async function searchRooms(evt) {
    evt.preventDefault();
    bookingAlert.innerHTML = '';

    const payload = collectPayload();
    lastSearchPayload = payload;
    setStatus('Dang tim phong phu hop...', 'warn');
    if (searchBtn) {
      searchBtn.disabled = true;
      searchBtn.textContent = 'Dang tim...';
    }

    try {
      const res = await fetch('/api/hotels/search?' + toQueryString(payload));
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data && data.error ? data.error : 'Khong the tim phong');
      }
      renderResults(data);
      setStatus('Tim thay ' + data.length + ' chi nhanh phu hop.', 'ok');
    } catch (err) {
      branchResults.innerHTML = '';
      setStatus('Loi tim kiem: ' + err.message, 'danger');
      setSummary(0, 0, null);
    } finally {
      if (searchBtn) {
        searchBtn.disabled = false;
        searchBtn.textContent = 'Tim phong trong ngay';
      }
    }
  }

  function resetSearchForm() {
    form.reset();
    toggleModeFields();
    branchResults.innerHTML = '<div class="note-card">Nhap bo loc va bam Tim phong trong ngay de hien ket qua.</div>';
    bookingAlert.innerHTML = '';
    setSummary(0, 0, null);
    setStatus('Da dat lai bo loc. San sang tim phong.', 'ok');
    lastSearchPayload = null;

    if (map) {
      markers.forEach((m) => map.removeLayer(m));
      markers = [];
      map.setView([16.5, 107.5], 6);
    }
  }

  function bookRoom(roomId, roomNumber, floorNumber, branchName) {
    if (!lastSearchPayload) {
      const recoveredPayload = collectPayload();
      if (hasRequiredSearchFields(recoveredPayload)) {
        lastSearchPayload = recoveredPayload;
      } else {
        bookingAlert.innerHTML = '<div class="alert-inline warn">Hay tim phong truoc khi dat.</div>';
        return;
      }
    }

    selectedRoomIdEl.value = roomId;
    selectedRoomLabelEl.textContent = 'Chi nhanh: ' + branchName + ' | Phong ' + roomNumber + ' - Tang ' + floorNumber;
    customerFullNameEl.value = '';

    if (!bookingModal) {
      bookingAlert.innerHTML = '<div class="alert-inline warn">Khong mo duoc hop thoai dat phong. Hay tai lai trang va thu lai.</div>';
      return;
    }

    bookingModal.show();
  }

  function handleBranchResultsClick(evt) {
    const btn = evt.target.closest('.js-book-room');
    if (!btn) {
      return;
    }

    const roomId = Number(btn.dataset.roomId || 0);
    const roomCtx = roomContextById[String(roomId)];

    if (!roomCtx) {
      bookingAlert.innerHTML = '<div class="alert-inline warn">Khong lay duoc thong tin phong da chon. Hay tim phong lai.</div>';
      return;
    }

    bookRoom(roomId, roomCtx.roomNumber, roomCtx.floorNumber, roomCtx.branchName || '');
  }

  async function submitBooking() {
    const roomId = Number(selectedRoomIdEl.value);
    const customerFullName = customerFullNameEl.value;

    if (!lastSearchPayload || !hasRequiredSearchFields(lastSearchPayload)) {
      bookingAlert.innerHTML = '<div class="alert-inline warn">Thong tin tim kiem khong hop le. Hay tim phong lai truoc khi dat.</div>';
      return;
    }

    if (!roomId) {
      bookingAlert.innerHTML = '<div class="alert-inline warn">Chua chon phong hop le.</div>';
      return;
    }

    if (!customerFullName || !customerFullName.trim()) {
      customerFullNameEl.focus();
      return;
    }

    const bookingPayload = {
      roomId: roomId,
      rentalMode: lastSearchPayload.rentalMode,
      customerFullName: customerFullName.trim(),
      voucherCode: lastSearchPayload.voucherCode
    };

    if (lastSearchPayload.rentalMode === 'HOURLY') {
      bookingPayload.startDateTime = lastSearchPayload.startDateTime;
      bookingPayload.endDateTime = lastSearchPayload.endDateTime;
    } else {
      bookingPayload.startDate = lastSearchPayload.startDate;
      bookingPayload.endDate = lastSearchPayload.endDate;
    }

    try {
      const res = await fetch('/api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bookingPayload)
      });

      const data = await res.json();

      if (res.status === 401 || res.status === 403) {
        bookingAlert.innerHTML = '<div class="alert-inline warn">Ban can dang nhap de dat phong. <a href="/login">Dang nhap ngay</a>.</div>';
        return;
      }

      if (!res.ok) {
        throw new Error(data && data.error ? data.error : 'Dat phong that bai');
      }

      bookingAlert.innerHTML = '<div class="alert-inline ok">Dat phong thanh cong! Ma booking #' + data.bookingId + ', tong tien ' + formatCurrency(data.totalPrice) + '.</div>';
      if (bookingModal) {
        bookingModal.hide();
      }
    } catch (err) {
      bookingAlert.innerHTML = '<div class="alert-inline danger">Dat phong that bai: ' + err.message + '</div>';
    }
  }

  rentalModeEl.addEventListener('change', toggleModeFields);
  form.addEventListener('submit', searchRooms);
  branchResults.addEventListener('click', handleBranchResultsClick);
  confirmBookingBtn.addEventListener('click', submitBooking);
  if (resetSearchBtn) {
    resetSearchBtn.addEventListener('click', resetSearchForm);
  }

  useLocationBtn.addEventListener('click', function () {
    if (!navigator.geolocation) {
      setStatus('Trinh duyet khong ho tro lay vi tri.', 'warn');
      return;
    }

    navigator.geolocation.getCurrentPosition(function (pos) {
      document.getElementById('userLatitude').value = pos.coords.latitude.toFixed(6);
      document.getElementById('userLongitude').value = pos.coords.longitude.toFixed(6);
      setStatus('Da lay vi tri hien tai.', 'ok');
    }, function () {
      setStatus('Khong lay duoc vi tri. Ban co the nhap tay.', 'warn');
    });
  });

  toggleModeFields();
  setSummary(0, 0, null);
})();
