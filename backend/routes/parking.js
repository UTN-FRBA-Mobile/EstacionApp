const router = require("express").Router();
const { getParkings } = require("../controllers/parking");

/************
    GET
*************/
router.get("/", getParkings);

/************
  POST
 *************/

module.exports = router;
