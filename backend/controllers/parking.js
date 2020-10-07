const getParkings = (req, res) => {
  console.log(req.io);
  res.status(200).send({ name: "Asd", sarasa: "ASD" });
};

module.exports = {
  getParkings,
};
