const ACCESS_KEY = "Dsk5Adot27q56wpyCMMmns2cnSTYoKZUwOiDD1nh3QM";
const destinations = ["Mysore", "Mangalore", "Bangalore", "Madikeri", "Chikmagalur", "Hampi"];
const cardsDiv = document.getElementById("cards");



// ===== WISHLIST STORAGE =====
let wishlist = JSON.parse(localStorage.getItem("wishlist")) || [];

function toggleWishlist(place, btn, icon) {
  if (wishlist.includes(place)) {
    wishlist = wishlist.filter(item => item !== place);
    btn.classList.remove("active");
    icon.classList.replace("fa-solid", "fa-regular");
  } else {
    wishlist.push(place);
    btn.classList.add("active");
    icon.classList.replace("fa-regular", "fa-solid");
  }

  localStorage.setItem("wishlist", JSON.stringify(wishlist));
  renderWishlistDropdown();

}

// ===== CARD SCROLL OBSERVER =====
const observer = new IntersectionObserver(
  (entries, obs) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add("show");
        obs.unobserve(entry.target);
      }
    });
  },
  { threshold: 0.2 }
);

destinations.forEach((place, index) => {
  fetch(`https://api.unsplash.com/search/photos?query=${place}&per_page=1&client_id=${ACCESS_KEY}`)
    .then(res => res.json())
    .then(data => {
      const img =
        data.results[0]?.urls?.regular ||
        "https://images.unsplash.com/photo-1488646953014-85cb44e25828";

      const card = document.createElement("div");
      card.className = "card";
      card.style.transitionDelay = `${index * 0.1}s`; // stagger

      card.innerHTML = `
        <img src="${img}" alt="${place}">
        <div class="card-content">
          <h3>${place}</h3>
		  <p class="wishlist-btn">
		    <span class="heart">
		      <i class="fa-regular fa-heart"></i>
		    </span>
		    Add to Wishlist
		  </p>


        </div>
      `;

      cardsDiv.appendChild(card);
	  
	  // ===== WISHLIST CLICK + RESTORE STATE =====
	  const wishlistBtn = card.querySelector(".wishlist-btn");
	  const heartIcon = wishlistBtn.querySelector("i");

	  // Restore wishlist state on reload
	  if (wishlist.includes(place)) {
	    wishlistBtn.classList.add("active");
	    heartIcon.classList.replace("fa-regular", "fa-solid");
	  }

	  // Instagram-like click behavior
	  wishlistBtn.addEventListener("click", () => {
	    toggleWishlist(place, wishlistBtn, heartIcon);

	    // 🔥 FORCE HEART BOUNCE (INSTAGRAM STYLE)
		// 🔥 FORCE HEART BOUNCE (INSTAGRAM STYLE)
		const heart = wishlistBtn.querySelector(".heart");

		heart.classList.remove("pop");   // reset animation
		void heart.offsetWidth;          // force reflow
		heart.classList.add("pop");      // play animation

	  });


      // 🔥 OBSERVE HERE (THIS WAS MISSING)
	  // stagger reveal
	  card.style.transitionDelay = `${index * 0.12}s`;
	  observer.observe(card);

    })
    .catch(err => console.error("Error:", err));
});


// ===== NAVBAR SCROLL SHADOW =====
const navbar = document.querySelector(".navbar");

window.addEventListener("scroll", () => {
  if (window.scrollY > 10) {
    navbar.classList.add("scrolled");
  } else {
    navbar.classList.remove("scrolled");
  }
});


// ===== WISHLIST DROPDOWN LOGIC =====
const wishlistNav = document.getElementById("wishlist-nav");
const wishlistDropdown = document.getElementById("wishlist-dropdown");

function renderWishlistDropdown() {
  wishlistDropdown.innerHTML = "";

  if (wishlist.length === 0) {
    wishlistDropdown.innerHTML =
      `<div class="empty-wishlist">No wishlist items yet</div>`;
    return;
  }

  wishlist.forEach(place => {
    const item = document.createElement("div");
    item.className = "wishlist-item";
    item.textContent = place;
    wishlistDropdown.appendChild(item);
  });
}

wishlistNav.addEventListener("click", (e) => {
  e.stopPropagation();
  wishlistDropdown.classList.toggle("active");
  renderWishlistDropdown();
});

document.addEventListener("click", () => {
  wishlistDropdown.classList.remove("active");
});



