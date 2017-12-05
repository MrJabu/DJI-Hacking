import math
import time
from droneapi.lib import VehicleMode, Location
from pymavlink import mavutil

api = local_connect()
vehicle = api.get_vehicles()[0]

def arm_and_takeoff(aTargetAltitude):
    """
    Arms vehicle and fly to aTargetAltitude.
    """

    print "Basic pre-arm checks"
    # Don't let the user try to fly autopilot is booting
    if vehicle.mode.name == "INITIALISING":
        print "Waiting for vehicle to initialise"
        time.sleep(1)
    while vehicle.gps_0.fix_type < 2:
        print "Waiting for GPS...:", vehicle.gps_0.fix_type
        time.sleep(1)
		
    print "Arming motors"
    # Copter should arm in GUIDED mode
    vehicle.mode    = VehicleMode("GUIDED")
    vehicle.armed   = True
    vehicle.flush()

    while not vehicle.armed and not api.exit:
        print " Waiting for arming..."
        time.sleep(1)

    print "Taking off!"
    vehicle.commands.takeoff(aTargetAltitude) # Take off to target altitude
    vehicle.flush()

    # Wait until the vehicle reaches a safe height before processing the goto (otherwise the command 
    #  after Vehicle.commands.takeoff will execute immediately).
    while not api.exit:
        print " Altitude: ", vehicle.location.alt
        if vehicle.location.alt>=aTargetAltitude*0.95: #Just below target, in case of undershoot.
            print "Reached target altitude"
            break;
        time.sleep(1)


# Vincenty's Direct formulae
def vinc_pt(phi1, lembda1, alpha12, s ) :
   """
   Returns the lat and long of projected point and reverse azimuth
   given a reference point and a distance and azimuth to project.
   lats, longs and azimuths are passed in decimal degrees
   Returns ( phi2,  lambda2,  alpha21 ) as a tuple

   f = flattening of the ellipsoid: 1/298.277223563
   a = length of the semi-major axis (radius at equator: 6378137.0)
   phi1 = latitude of the starting point
   lembda1 = longitude of the starting point
   alpha12 = azimuth (bearing) at the starting point
   s = length to project to next point
   """

   f = 1/298.277223563
   a = 6378137.0

   piD4 = math.atan( 1.0 )
   two_pi = piD4 * 8.0
   phi1    = phi1    * piD4 / 45.0
   lembda1 = lembda1 * piD4 / 45.0
   alpha12 = alpha12 * piD4 / 45.0
   if ( alpha12 < 0.0 ) :
      alpha12 = alpha12 + two_pi
   if ( alpha12 > two_pi ) :
      alpha12 = alpha12 - two_pi

   # length of the semi-minor axis (radius at the poles)
   b = a * (1.0 - f)
   TanU1 = (1-f) * math.tan(phi1)
   U1 = math.atan( TanU1 )
   sigma1 = math.atan2( TanU1, math.cos(alpha12) )
   Sinalpha = math.cos(U1) * math.sin(alpha12)
   cosalpha_sq = 1.0 - Sinalpha * Sinalpha

   u2 = cosalpha_sq * (a * a - b * b ) / (b * b)
   A = 1.0 + (u2 / 16384) * (4096 + u2 * (-768 + u2 * \
      (320 - 175 * u2) ) )
   B = (u2 / 1024) * (256 + u2 * (-128 + u2 * (74 - 47 * u2) ) )

   # Starting with the approx
   sigma = (s / (b * A))
   last_sigma = 2.0 * sigma + 2.0   # something impossible

   # Iterate the following 3 eqs unitl no sig change in sigma
   # two_sigma_m , delta_sigma
   while ( abs( (last_sigma - sigma) / sigma) > 1.0e-9 ) :

      two_sigma_m = 2 * sigma1 + sigma
      delta_sigma = B * math.sin(sigma) * ( math.cos(two_sigma_m) \
            + (B/4) * (math.cos(sigma) * \
            (-1 + 2 * math.pow( math.cos(two_sigma_m), 2 ) -  \
            (B/6) * math.cos(two_sigma_m) * \
            (-3 + 4 * math.pow(math.sin(sigma), 2 )) *  \
            (-3 + 4 * math.pow( math.cos (two_sigma_m), 2 ))))) \

      last_sigma = sigma
      sigma = (s / (b * A)) + delta_sigma

   phi2 = math.atan2 ( (math.sin(U1) * math.cos(sigma) + math.cos(U1) * math.sin(sigma) * math.cos(alpha12) ), \
      ((1-f) * math.sqrt( math.pow(Sinalpha, 2) +
      pow(math.sin(U1) * math.sin(sigma) - math.cos(U1) * math.cos(sigma) * math.cos(alpha12), 2))))

   lembda = math.atan2( (math.sin(sigma) * math.sin(alpha12 )), (math.cos(U1) * math.cos(sigma) -
      math.sin(U1) *  math.sin(sigma) * math.cos(alpha12)))

   C = (f/16) * cosalpha_sq * (4 + f * (4 - 3 * cosalpha_sq ))
   omega = lembda - (1-C) * f * Sinalpha *  \
      (sigma + C * math.sin(sigma) * (math.cos(two_sigma_m) +
      C * math.cos(sigma) * (-1 + 2 * math.pow(math.cos(two_sigma_m),2) )))

   lembda2 = lembda1 + omega
   alpha21 = math.atan2 ( Sinalpha, (-math.sin(U1) * math.sin(sigma) +
      math.cos(U1) * math.cos(sigma) * math.cos(alpha12)))

   alpha21 = alpha21 + two_pi / 2.0
   if ( alpha21 < 0.0 ) :
      alpha21 = alpha21 + two_pi
   if ( alpha21 > two_pi ) :
      alpha21 = alpha21 - two_pi

   phi2       = phi2       * 45.0 / piD4
   lembda2    = lembda2    * 45.0 / piD4
   alpha21    = alpha21    * 45.0 / piD4
#   return phi2,  lembda2,  alpha21
   return phi2,  lembda2 # We don't need the final Azimuth in this case. Just the lat / lon pairs. 

# inputs
radius = 15.0 # m - the following code is an approximation that stays reasonably accurate for distances < 100km
centerLat = 40.12076
# latitude of circle center, decimal degrees
centerLon = -83.07773
# Longitude of circle center, decimal degrees

# parameters
N = 10 # number of discrete sample points to be generated along the circle

# generate points
circlePoints = []

for k in xrange(N):
    # compute
    angle = math.pi*2*k/N
    dx = radius*math.cos(angle)
    dy = radius*math.sin(angle)
    point = {}
    point['lat']=centerLat + (180/math.pi)*(dy/6378137)
    point['lon']=centerLon + (180/math.pi)*(dx/6378137)/math.cos(centerLat*math.pi/180)
    # add to list
    circlePoints.append(point)

#print circlePoints

#print "Initial generated circle from provided center point"
#for point in circlePoints:
#    print (point['lat'],point['lon'])

circlePoints = [
#Penis NOT circle!
(40.11975, -83.07676), 
(40.1204, -83.07792), 
(40.12036, -83.07794), 
(40.12034, -83.07797),
(40.12037, -83.07801),
(40.12047, -83.07803), 
(40.12053, -83.07798), 
(40.12054, -83.07793), 
(40.12051, -83.0779), 
(40.12049, -83.07789), 
(40.12045, -83.07786), 
(40.12042, -83.07786), 
(40.1204, -83.07783), 
(40.12038, -83.0778), 
(40.12039, -83.07775), 
(40.12046, -83.0777), 
(40.12058, -83.07772), 
(40.12061, -83.07776), 
(40.12061, -83.0778), 
(40.1206, -83.07784), 
(40.12058, -83.07786), 
(40.1207, -83.07786), 
(40.1208, -83.07785), 
(40.12091, -83.07785), 
(40.12102, -83.07787), 
(40.12105, -83.07793), 
(40.12101, -83.07798), 
(40.12096, -83.07798), 
(40.1209, -83.07799), 
(40.12084, -83.07799), 
(40.12079, -83.07798), 
(40.12073, -83.07797), 
(40.12068, -83.07797), 
(40.12064, -83.07796)]

def bearing(pointA, pointB):

    # Calculates the bearing between two points.
    #
    # :Parameters:
    #   - `pointA: The tuple representing the latitude/longitude for the
    #     first point. Latitude and longitude must be in decimal degrees
    #   - `pointB: The tuple representing the latitude/longitude for the
    #     second point. Latitude and longitude must be in decimal degrees
    #
    # :Returns:
    #   The bearing in degrees
    #
    # :Returns Type:
    #   float

    # if (type(pointA) != tuple) or (type(pointB) != tuple):
    #     raise TypeError("Only tuples are supported as arguments")

    lat1 = math.radians(pointA[0])
    lat2 = math.radians(pointB[0])

    diffLong = math.radians(pointB[1] - pointA[1])

    x = math.sin(diffLong) * math.cos(lat2)
    y = math.cos(lat1) * math.sin(lat2) - (math.sin(lat1)
            * math.cos(lat2) * math.cos(diffLong))

    initial_bearing = math.atan2(x, y)

    # Now we have the initial bearing but math.atan2 return values
    # from -180 to + 180 which is not what we want for a compass bearing
    # The solution is to normalize the initial bearing as shown below
    initial_bearing = math.degrees(initial_bearing)
    compass_bearing = (initial_bearing + 360) % 360

    return compass_bearing


# Point #1 - 40.12076, -83.07773
# https://www.google.com/maps/dir/40.12083920247139,++-83.07758744139487/40.12076,-83.0775538/40.120888152290696,+-83.07767554745823/40.120888152290696,++-83.07778445254178/40.12083920247139,+-83.07787255860514/40.12076,++-83.07790621212672/40.12068079752861,++-83.07787255860514/40.1206318477093,+-83.07778445254178/40.1206318477093,+-83.07767554745823/40.12076,+-83.07773/@40.1205266,-83.080423,17z/data=!3m1!4b1!4m53!4m52!1m5!1m1!1s0x0:0x0!2m2!1d-83.0775874!2d40.1208392!1m0!1m5!1m1!1s0x0:0x0!2m2!1d-83.0776755!2d40.1208882!1m5!1m1!1s0x0:0x0!2m2!1d-83.0777845!2d40.1208882!1m5!1m1!1s0x0:0x0!2m2!1d-83.0778726!2d40.1208392!1m5!1m1!1s0x0:0x0!2m2!1d-83.0779062!2d40.12076!1m5!1m1!1s0x0:0x0!2m2!1d-83.0778726!2d40.1206808!1m5!1m1!1s0x0:0x0!2m2!1d-83.0777845!2d40.1206318!1m3!2m2!1d-83.0776755!2d40.1206318!1m3!2m2!1d-83.07773!2d40.12076!3e2
#
#Point #2 - 40.120662, -83.078250

# Goalpost for tip of penis 40.12180 -83.07698

print "Bearing "  
#b = bearing((40.12076, -83.07773), (40.120662, -83.078250))
b = bearing((40.12076, -83.07773), (40.12180,-83.07698))
print b 

from math import radians, cos, sin, asin, sqrt

def haversine(lon1, lat1, lon2, lat2):
    """
    Calculate the great circle distance between two points 
    on the earth (specified in decimal degrees)
    """
    # convert decimal degrees to radians 
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])

    # haversine formula 
    dlon = lon2 - lon1 
    dlat = lat2 - lat1 
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a)) 
    r = 6371 # Radius of earth in kilometers. Use 3956 for miles
    return c * r


# Old center to new center
h = haversine(40.12076, -83.07773, 40.120662, -83.078250)
print h 

arm_and_takeoff(20)

print "Secondary calculated circle (polygon) from haversinve data + vincenty direct vs. new center point"
for point in circlePoints:
    print "Going to point..."
    print str(vinc_pt(point[0], point[1], b, h*1000)) 
    point1 = Location(point[0], point[1], 20, is_relative=True)
    vehicle.commands.goto(point1)
    vehicle.flush()

    # sleep so we can see the change in map
    time.sleep(20)

# sleep so we can see the change in map
time.sleep(10)

print "Returning to Launch"
vehicle.mode    = VehicleMode("RTL")
vehicle.flush()


