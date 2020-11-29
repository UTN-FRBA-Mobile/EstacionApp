const { PARKING_ROOM } = process.env;
const locations = [
  {
    id: 1,
    longitude: -58.5824661,
    latitude: -34.6055728,
    photos: [
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
    ],
  },
  {
    id: 2,
    latitude: -34.586544,
    longitude: -58.576842,
    photos: [
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
    ],
  },
  {
    id: 3,
    latitude: -34.58662,
    longitude: -58.573194,
    photos: [
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
    ],
  },
  {
    id: 4,
    latitude: -34.5857789,
    longitude: -58.5800094,
    photos: [
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
      "https://www.centediario.com/wp-content/uploads/2016/06/jardin-baleado.jpg",
    ],
  },
  {
    id: 5,
    latitude: -34.584153,
    longitude: -58.579868,
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
    let errors = [];

    if (!latitude)
      errors.push({ field: "latitude", error: "Complete la latitud" });
    if (!longitude)
      errors.push({ field: "longitude", error: "Complete la longitude" });
    if (!id) errors.push({ field: "id", error: "Complete el id" });
    if (!photos) errors.push({ field: "photos", error: "Complete las fotos" });

    res.status(400).send({ success: false, errors });
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
