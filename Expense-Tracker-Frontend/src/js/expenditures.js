const serverUri = "http://localhost:8080";
const expenditureList = document.getElementById("expenditureList");
const template = document.getElementById("expenditureItemTemplate");

const addForm = document.getElementById("addForm");
const addTitle = document.getElementById("addTitle");
const addAmount = document.getElementById("addAmount");

const editForm = document.getElementById("editForm");
const editTitle = document.getElementById("editTitle");
const editAmount = document.getElementById("editAmount");
const editId = document.getElementById("editId");

function loadExpenditures() {
  fetch(serverUri + "/api/expenditures", {
    credentials: "include",
    })
    .then(res => res.ok ? res.json() : Promise.reject("Failed to fetch"))
    .then(data => {
      expenditureList.innerHTML = "";
      data.forEach(addExpenditureToDOM);
    })
    .catch(err => alert("Error: " + err));
}

function addExpenditureToDOM(exp) {
  const clone = template.content.cloneNode(true);
  const li = clone.querySelector("li");
  li.dataset.id = exp.id;

  const title = clone.querySelector(".expenditure-title");
  const amount = clone.querySelector(".expenditure-amount");
  title.textContent = exp.title;
  amount.textContent = `Amount: Rs. ${exp.amount}`;

  const editBtn = clone.querySelector(".edit-btn");
  const deleteBtn = clone.querySelector(".delete-btn");

  editBtn.onclick = () => {
    editId.value = exp.id;
    editTitle.value = exp.title;
    editAmount.value = exp.amount;
    new bootstrap.Modal(document.getElementById("editModal")).show();
  };

  deleteBtn.onclick = () => {
    if (confirm("Are you sure you want to delete this expenditure?")) {
      fetch(`${serverUri}/api/expenditures/${exp.id}`, {
        method: "DELETE",
        credentials: "include"
      })
        .then(res => {
          if (res.ok) li.remove();
          else throw new Error("Delete failed");
        })
        .catch(err => alert("Error: " + err));
    }
  };

  expenditureList.appendChild(clone);
}

addForm.onsubmit = e => {
  e.preventDefault();

  const data = {
    title: addTitle.value,
    amount: parseFloat(addAmount.value)
  };

  fetch(serverUri + "/api/expenditures", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(data)
  })
    .then(async response => {
      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Expenditure NOT added!");
      }
      return response.json();
    })
    .then(exp => {
      addForm.reset();
      bootstrap.Modal.getInstance(document.getElementById("addModal")).hide();
      addExpenditureToDOM(exp);
    })
    .catch(err => alert("Error: " + err.message));
};


editForm.onsubmit = async e => {
  e.preventDefault();
  const id = editId.value;

  const res = await fetch(serverUri + '/api/users', { credentials: 'include' });
  const userData = await res.json();

  const data = {
      title: editTitle.value,
      amount: parseFloat(editAmount.value),
      user: userData.username,
    };

  fetch(`${serverUri}/api/expenditures/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(data)
  })
    .then(res => res.ok ? res.json() : Promise.reject("Update failed"))
    .then(() => {
      bootstrap.Modal.getInstance(document.getElementById("editModal")).hide();
      loadExpenditures();
    })
    .catch(err => alert("Error: " + err));
};

loadExpenditures();


