const socket = io.connect("http://localhost:8080", {transports: ['websocket']});
let registered = false;


socket.on('gameState', function (data) {
    // console.log("Updating game... with message: " + data);
    update(data);
});

socket.on('init', function (data) {
    // console.log("Initializing game... with message: " + data);
    initGame(data);
});
function initGame(data) {
    let div = document.getElementById("buyButtons");
    div.innerHTML = "";
    data = JSON.parse(data);
    let equipm = data["equipment"];
    for (let i = 0; i < equipm.length; i++) {
        // console.log(equipm[i]["id"])
        div.innerHTML += "<button onclick=\"buy('" + equipm[i]["id"]  + "')\"> Buy " + equipm[i]["name"] + "</button> "
    }
}

function hack100(){
    socket.emit("hack100")
}

function hack1000(){
    socket.emit("hack1000")
}

function update(data) {
    data = JSON.parse(data);
    updateEquipment(data);
    updateGold(data.gold);
    // console.log(data);
}

// Also updates username and last update time
function updateEquipment(data) {
    let equipmentDiv = document.getElementById("equipmentList");
    equipmentDiv.innerHTML = "";
    let equipment = data.equipment;
    for (let key in equipment) {
        let element = equipment[key];
        equipmentDiv.innerHTML +=
        "<li>" + element.id +
            "<ul>" + "" +
                "<li>Price: <span class=\"price\">" + element.cost + "</span></li>" +
                "<li>Number owned: <span class=\"numOwned\">" + element.numberOwned + "</span></li>" +
            "</ul>" +
        "</li>"
    }

    let dataDiv = document.getElementById("lastUpdateTime");
    dataDiv.innerHTML = "Last update time: " + data['lastUpdateTime'];
    let usernameDiv = document.getElementById("usernameDisplay");
    usernameDiv.innerHTML = "Username: " + data['username'];
}

function updateGold(gold) {
    document.getElementById("currentGold").innerText = gold;
}

function register() {
    let username = document.getElementById("username").value;
    socket.emit("register", username);
    document.getElementById("registration").innerHTML = "";
    registered = true;
}

function clickGold() {
    if (registered) {
        socket.emit("clickGold");
    }
}

function buy(equipmentID) {
    if (registered) {
        socket.emit("buy", equipmentID);
    }
}

