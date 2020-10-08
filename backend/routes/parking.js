const router = require("express").Router();
const { postParkings } = require("../controllers/parking");

/************
    GET
*************/

/************
 POST
 *************/
router.post("/", postParkings);

module.exports = router;
