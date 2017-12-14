
CREATE INDEX Passengeri
ON    Passenger 
USING BTREE(pID) ;

CREATE INDEX Flighti
ON    Flight 
USING BTREE(flightNum) ;

CREATE INDEX Bookingi
ON    Booking 
USING BTREE(pid);
