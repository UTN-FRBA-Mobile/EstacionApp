const router = require("express").Router();
const { postParkings, getParkings, deleteParking } = require("../controllers/parking");

router.get("/", getParkings);

router.post("/", postParkings);

router.delete("/:id", deleteParking);

module.exports = router;
