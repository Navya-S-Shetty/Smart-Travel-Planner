// 1. CONFIGURATION: Use your Unsplash Access Key here
const UNSPLASH_ACCESS_KEY = 'Dsk5Adot27q56wpyCMMmns2cnSTYoKZUwOiDD1nh3QM'; 

const clusters = [
    { 
        id: 1, 
        name: "Coastal Soul", 
        categories: ["beaches", "temple"], 
        days: 3, 
        query: "Mangalore beach coastal view", 
        sub: "Mangalore, Udupi, Murudeshwar" 
    },
    { 
        id: 2, 
        name: "Ancient Stones", 
        categories: ["heritage", "temple"], 
        days: 4, 
        query: "Hampi stone chariot temple", 
        sub: "Hampi, Badami, Aihole" 
    },

    /* 🔥 SEPARATED COORG */
    { 
        id: 3, 
        name: "Coffee Trails", 
        categories: ["nature"], 
        days: 2, 
        query: "Coorg beauty", 
        sub: "Coorg" 
    },

    /* 🔥 SEPARATED CHIKMAGALUR */
    { 
        id: 4, 
        name: "Misty Peaks", 
        categories: ["nature"], 
        days: 2, 
        query: "Chikmagalur nature", 
        sub: "Chikmagalur" 
    },

    { 
        id: 5, 
        name: "Wild West", 
        categories: ["beaches", "adventure"], 
        days: 3, 
        query: "Dandeli", 
        sub: "Dandeli, Gokarna, Karwar" 
    },
    { 
        id: 6, 
        name: "Royal Deccan", 
        categories: ["heritage"], 
        days: 3, 
        query: "Mysore Palace", 
        sub: "Mysore, Belur, Halebidu" 
    },
    { 
        id: 7, 
        name: "The Metro Pulse", 
        categories: ["nature", "adventure"], 
        days: 2, 
        query: "Bangalore city", 
        sub: "Bangalore, Nandi Hills" 
    }
];

let bucket = [];

// 2. IMAGE FETCH: Same logic from your high-end Home Page
async function fetchImage(query) {
    try {
        const response = await fetch(`https://api.unsplash.com/search/photos?query=${query}&client_id=${UNSPLASH_ACCESS_KEY}&per_page=1`);
        const data = await response.json();
        // Return the first image or a reliable travel fallback
        return data.results[0]?.urls?.regular || "https://images.unsplash.com/photo-1590496793907-3997e993772a";
    } catch (err) {
        console.error("Unsplash Fetch Failed:", err);
        return "https://images.unsplash.com/photo-1590496793907-3997e993772a";
    }
}

// 3. CORE RENDER: Rebuild the grid with fresh images
async function renderGrid(filter) {
    const grid = document.getElementById('exploreGrid');
    grid.innerHTML = '<p style="color:white; grid-column: 1/-1; text-align:center;">Fetching curated images...</p>';
    
    const filtered = filter === 'all' 
        ? clusters 
        : clusters.filter(c => c.categories.includes(filter));

    // Wait for all images to load before displaying
    const cardsHtml = await Promise.all(filtered.map(async (c) => {
        const imageUrl = await fetchImage(c.query);
        c.img = imageUrl; // Keep this image for the bucket thumbnail
		return `
		    <div class="p-card">
		        <img src="${imageUrl}" alt="${c.name}">
		        <div class="card-overlay">
		            <h3>${c.name}</h3>
		            <p><i class="fa-solid fa-location-dot"></i> ${c.sub}</p>
		            <div class="card-actions" style="display: flex; gap: 10px; margin-top: 10px;">
		                
					<button class="explore-btn"
					    onclick="handleExplore('${c.name}')">
					    Explore & Plan
					</button>

		            </div>
		        </div>
		    </div>
		`;
    }));

    grid.innerHTML = cardsHtml.join('');
}

// 4. INITIAL LOAD: Connect with Wishlist
// PASTE THIS NEW BLOCK:
window.onload = async () => {
    // 1. Check User Session & Handle Logout Button
    try {
        const response = await fetch('/api/user/me');
        const name = await response.text();
        
        const usernameDisplay = document.getElementById('usernameDisplay');
        const logoutBtn = document.getElementById('logoutBtn');

        if (name && name !== "Guest Planner") {
            // If logged in, show name and logout button
            usernameDisplay.innerHTML = `<i class="fa-solid fa-user-circle"></i> ${name}`;
            if (logoutBtn) logoutBtn.style.display = "inline-block";
        } else {
            // If guest, hide logout button
            usernameDisplay.innerHTML = `<i class="fa-solid fa-user-circle"></i> Guest Planner`;
            if (logoutBtn) logoutBtn.style.display = "none";
        }
    } catch (err) {
        console.log("Not logged in.");
    }

    // 2. Existing Wishlist & Grid Logic
    const savedWishlist = JSON.parse(localStorage.getItem('userWishlist')) || [];
    clusters.forEach(c => {
        if (savedWishlist.some(wish => c.sub.includes(wish) || c.name.includes(wish))) {
            addToBucket(c.id);
        }
    });
    
    renderGrid('all');
};

function addToBucket(id) {
    const item = clusters.find(c => c.id === id);
    if (!bucket.find(b => b.id === id)) {
        bucket.push(item);
        updateBucketUI();
    }
}

function updateBucketUI() {
    const bucketDiv = document.getElementById('bucketItems');
    const totalDays = bucket.reduce((sum, item) => sum + item.days, 0);
    document.getElementById('totalDays').innerText = totalDays;
    
    bucketDiv.innerHTML = bucket.map(item => `
        <div class="mini-card">
            <img src="${item.img}">
            <span>${item.name}</span>
            <i class="fa-solid fa-xmark" style="cursor:pointer" onclick="removeFromBucket(${item.id})"></i>
        </div>
    `).join('');
}

function removeFromBucket(id) {
    bucket = bucket.filter(b => b.id !== id);
    updateBucketUI();
}

// 5. FILTER CLICKS
document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.onclick = () => {
        document.querySelector('.filter-btn.active').classList.remove('active');
        btn.classList.add('active');
        renderGrid(btn.dataset.filter);
    };
});

// Function to connect Explore Page to Plan Details Page
function goToPlanDetails(cityName) {
    // Adding the / at the start is the key fix
    window.location.href = `/plan-details.html?city=${encodeURIComponent(cityName)}`;
}


function handleExplore(clusterName) {

	    const mapping = {
	        "Coastal Soul": "Coastal Karnataka",
	        "Coffee Trails": "Coorg",
			"Ancient Stones": "Hampi",
	        "Misty Peaks": "Chikkamagaluru",
	        "Wild West": "Uttara Kannada",
	        "Royal Deccan": "Mysore",
	        "The Metro Pulse": "Bangalore"
	    };

	    const city = mapping[clusterName];
	    goToPlanDetails(city);
	}
	function syncWithBackend() {
	    if (bucket.length === 0) {
	        alert("Your bucket is empty! Add some destinations first.");
	        return;
	    }

	    const firstCity = bucket[0].sub.split(',')[0];
	    goToPlanDetails(firstCity);
	}

