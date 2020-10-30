const { PARKING_ROOM } = process.env;
const locations = [
  {
    id: 1,
    longitude: -58.5824661,
    latitude: -34.6055728,
    reserved: false,
    photos: [
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
    ],
  },
];

const postParkings = (
  { app, body: { id, latitude, longitude, photos } },
  res
) => {
  const io = app.get("socketio");

  if (latitude && longitude && id && photos) {
    const newLocation = { id, latitude, longitude, photos, reserved: false };
    const location = locations.find((location) => location.id === id);
    location ? (location.reserved = false) : locations.push(newLocation);
    io.to(PARKING_ROOM).emit("new_locations", newLocation);
    res.status(201).send({ success: true, data: "Parking added" });
  } else {
    res.status(400).send({ success: false, data: "Wrong body" });
  }
};

const deleteParking = ({ app, params: { id } }, res) => {
  const io = app.get("socketio");

  if (id) {
    const deletedLocation = locations.find((location) => location.id === +id);
    if (!deletedLocation) {
      res.status(404).send({ success: false, data: "Not found" });
      return;
    }
    deletedLocation.reserved = true;
    io.to(PARKING_ROOM).emit("deleted_locations", {
      latitude: deletedLocation.latitude,
      longitude: deletedLocation.longitude,
    });
    res.status(200).send({ success: true, data: "Parking deleted" });
  } else {
    res.status(400).send({ success: false, data: "Wrong body" });
  }
};

const getParkings = (req, res) => {
  res.status(200).send({ locations });
};

module.exports = {
  postParkings,
  deleteParking,
  getParkings,
  locations,
};
