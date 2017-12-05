# Under Development

These docs represent a protocol that is still heavily under development - until the first internal implementations are done, it would be unwise to start an independent implementation as I may change the protocol at any point until it's feature complete.

# Extending Kismet: Creating Capture Sources

Kismet supports additional capture types via the `KisDatasource` interface.  Data sources run in an independent process and can be written in any language, however they require a C++ component which functions as a Kismet driver to allow it to communicate with the datasource binary.

Datasources can report packets or complex records - if your datasource needs to pass parsed information about a device event, that's possible!

## Capture via IPC and Network

Kismet datasources communicate from the capture binary to the Kismet server via an IPC channel or TCP connection.  This channel passes commands, data, and msgpack binary objects via a simple wrapper protocol.

The datasource IPC channel is via inherited file descriptors:  Prior to launching the capture binary, the Kismet server makes a pipe(2) pair and will pass the read (incoming data to the capture binary) and write (outgoing data from the capture binary) file descriptor numbers on the command line of the capture binary.

Operating as a completely separate binary allows the capture code to use increased permissions via suid, operate independently of the Kismet main loop, allowing the use of alternate main loop methods or other processor-intensive operations which could stall the main Kismet packet loop, or even using other languages to define the capture binary, such as a python capture system which utilizes python radio libraries.

The network protocol is an encapsulation of the same protocol over a TCP channel, with some additional setup frames.  The network protocol will be more fully defined in future revisions of this document.

## The Simplified Datasource Protocol

The data source capture protocol is defined in `simple_datasource_proto.h`.  It is designed to be a simple protocol to communicate with from a variety of languages.

Each communication to or from the capture driver consists of a high-level frame type (a string), which then contains an arbitrary collection of key:value dictionary pairs.

K:V pairs may be simple (a single value or type), or complex (a binary msgpack object, for instance).

In general, complex objects are always passed as dictionaries holding string:value KV pairs.  This allows multiple languages easy access, eliminates magic number values, and should greatly simplify future compatibility issues if additional fields are required.

Datasource implementations *may* use other message passing mechanisms, either inside the established communications channel or via independent channels, but are encouraged to stay within the defined protocol whenever possible.

## Top-level Frame Types

Several top-level packet types and key:value pairs are pre-defined and will be automatically handled by classes derived from the `KisDataSource` driver.

#### CHANNELS (Datasource->Kismet)
Alert Kismet that the device has changed its channel list. 

This occurs when, for instance, a Wi-Fi card driver communicates that it supports some set of channels, but is unable to actually tune to them.  Sending this message will most likely cause the server to rebalance channel hopping.

KV Pairs:
* CHANNELS (representing the new *current channel list*, the *supported channel list* remains unchanged)

Responses:
* NONE

#### CLOSEDEVICE (Kismet->Datasource)
Close any open device and initiate a shutdown.  Sent to capture binary during source close or server shutdown.

KV Pairs:
* NONE

Responses:
* NONE

#### CONFIGURE (Kismet->Datasource)
Reconfigure a source.  Typically used to pass channel configuration data but may be used to embed additional information.

KV Pairs:
* CHANSET (optional)
* CHANHOP (optional)
* SPECSET (optional)

Responses:
* CONFIGRESP

#### CONFIGRESP (Datasource->Kismet)
Acknowledge a source has been reconfigured, and return the new configuration state.

KV Pairs:
* CHANSET (optional)
* CHANHOP (optional)
* SUCCESS (optional, datasource->kismet)
* MESSAGE (optional, datasource->kismet)
* WARNING (optional, datasource->kismet)

Responses:
* NONE

#### DATA (Datasource->Kismet)
Pass capture data.  May be a packet, a decoded trackable entity, or other information.

KV Pairs:
* GPS (optional)
* MESSAGE (optional)
* PACKET (optional)
* SIGNAL (optional)
* SPECTRUM (optional)
* WARNING (optional)

Responses:
* NONE

#### ERROR (Any)
An error occurred.  The capture is assumed closed, and the connection will be shut down.

KV Pairs:
* SUCCESS
* MESSAGE (optional)

Responses:
* NONE

#### LISTINTERFACES (Kismet->Datasource)
Request a list of interfaces.  This allows Kismet to present a list of compatible auto-detected interfaces, but not all datasource methods will support it.

KV Pairs:
* NONE

Responses:
* LISTRESP

#### LISTRESP (Datasource->Kismet)
Return a list of interfaces.

KV Pairs:
* SUCCESS
* MESSAGE (optional)
* INTERFACELIST (optional)

Responses:
* NONE

#### MESSAGE (Datasource->Kismet)
Message for the user - informational, warning, or other non-critical errors.  Essentially a tunneling of the Kismet Messagebus protocol.  Permanent failure conditions should be carried over the ERROR frames.

KV Pairs:
* MESSAGE
* WARNING

Responses:
* NONE

#### NEWSOURCE (Datasource->Kismet Network)
Sent from a datasource running in network mode (remote capture) to Kismet to tell it to create a source and attach it to the network socket.

The driver type must be included so that Kismet knows how to map the remote interface.

Kismet will respond by initiating a typical OPENDEVICE sequence to sync the state.

KV Pairs:
* DEFINITION
* SOURCETYPE
* UUID

Responses:
* OPENDEVICE
* ERROR

#### OPENDEVICE (Kismet->Datasource)
Open a device.  This should only be sent to a datasource which is capable of handling this device type, but may still return errors.

KV Pairs:
* DEFINITION

Responses:
* OPENRESP

#### OPENRESP (Datasource->Kismet)
Device open response.  Sent to declare the source is open and functioning, or that there was an error.

KV Pairs:
* CAPIF (optional)
* CHANNELS (optional)
* CHANSET (optional)
* DLT
* MESSAGE (optional)
* SPECSET (optional)
* SUCCESS
* UUID (optional)
* WARNING (optional)

Responses:
* NONE

#### PING (Kismet->Datasource and Datasource->Kismet)
Send a keep-alive frame to monitor that the remote capture is still functional.

Failure to receive a PONG response within 5 seconds indicates that a problem has occurred, and that the Kismet server should terminate the capture binary with an error, or that the capture binary should exit.

KV Pairs:
* None

Responses:
* PONG

#### PONG (Kismet->Datasource and Datasource->Kismet)
Respond to a keep-alive PING frame.

Failure to send a PONG response within 5 seconds indicates a problem has occurred.

KV Pairs:
* None

Responses:
* None

#### PROBEDEVICE (Kismet->Datasource)
Probe if this datasource can handle a device of unknown type.  This is used during the probing for auto-type sources.

KV Pairs:
* DEFINITION

Responses:
* PROBERESP

#### PROBERESP (Datasource->Kismet)
Response for attempting to probe if a device is supported via PROBEDEVICE.  This should always be sent, even if the answer is that the device is unsupported.

KV Pairs:
* SUCCESS
* MESSAGE (optional)
* CHANNELS (optional)
* CHANSET (optional)
* SPECSET (optional)

Responses:
* NONE

## Standard KV Pairs

Kismet will automatically handle standard KV pairs in a message.  A datasource may define arbitrary additional KV pairs and handle them independently.

#### CAPIF
Some capture sources may use an alternate interface for capturing - for instance, the Linux Wi-Fi capture system will make a VIF (virtual interface) to capture on most modern drivers, or a USB capture may use the absolute path to the USB interface being used.

Content:

Simple string `(char *)` of the alternate interface, length dictated by the KV length record.

Example:

`"capif": "wlan0mon"`

#### CHANNELS
Conveys a list of channels supported by this device, if there is a user presentable list for this phy type.  Channels are considered free-form strings which are unique to a phy type, but should be human readable.  Channel definitions may also represent frequencies in a form relevant to the phy, such as "2412MHz", but the representation is phy specific.

Content:

A msgpack packed dictionary of parameters containing the following:
* "channels": Vector of strings defining channels.

Example:

`"channels": ["1", "6", "11", "6HT20", "11HT40-"]` (802.11n complex channel definitions)

#### CHANSET
Used as a set command to configure a single, non-hopping channel.  Channels are free-form strings which are human readable and phy-specific.

Content:

Simple string `(char *)` of the channel, length dictated by the KV length record.

Example:

`"11HT20"`

`"2412MHz"`

#### CHANHOP
Used as a set command to configure hopping over a list of channels or frequencies.  The hop rate is sent as a double containing the number of hops per second, hop rates less than one are interpreted as multiple seconds per hop.

Content:

Msgpack packed dictionary of parameters containing at least the following:
* "channels": Vector of strings defining channels, as show in the `CHANNELS` KV pair.
* "rate": double-precision float indicating channels per second.
* "shuffle": uint8 boolean indicating the source should, at its discretion, shuffle the order of the channel hopping to take advantage of channel overlap where possible
* "shuffle_skip": uint32 boolean sent from the datasource to Kismet to communicate how many channels are skipped per hop.
* "offset": start at an offset in the channel hopping list; this is used to tell the source to start hopping at a position other than 0; this lets Kismet easily split hopping between sources which share a type.

Examples:

`{"channels": ["1", "6", "11"], "rate": 10}` (10 channels per second on primary 802.11 channels)

`{"channels": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"], "rate": 10, shuffle: 1, offset: 6}` (10 channels per second on 802.11 channels, tell the source to randomize to prevent overlap, and tell this source to start halfway through the list)

`{"channels": ["3", "6", "9"], "rate": 0.16}` (10 *seconds per channel* on alternate 802.11 channels, caused by a rate of 0.1 channels per second.)

#### DEFINITION
A raw source definition, as a string.  This is identical to the source as defined in `kismet.conf` or on the Kismet command line.

Content:

Simple string `(char *)` of the definition, length dictated by the KV length record.

Example:

`wlan0:hop=true,name=foobar`

#### DLT
Indicates the DLT (Data Link Type) of the capture interface; typically as reported by libpcap.

Content:

Simple `uint32_t` of the DLT.


#### GPS
If a driver contains its own location information (or is running on a remote system which has its own GPS), captured data may be tagged with GPS information.  This is not necessary when reporting data or device information with inherent location information (such as PPI+GPS packets, or some other phy type which embeds positional information in packets).

The GPS values are inserted into the packet on the Kismet level as a standard location record.

A GPS record is inserted into the Kismet packet as a "GPS" record.

Content:

Msgpack packed dictionary containing at least the following values:
* "lat": double-precision float containing latitude
* "lon": double-precision float containing logitude
* "alt": double-precision float containing altitude, in meters
* "speed": double-precision float containing speed, in kilometers per hour
* "heading": double-precision float containing the heading in degrees (optional)
* "precision": double-precision float containing the coordinate precision in meters (optional)
* "fix": int32 integer containing the "fix" quality (0 = none, 2 = 2d, 3 = 3d)
* "time": uint64 containing the time in seconds since the epoch (time_t record)
* "type": string containing the GPS type
* "name": string containing the GPS user-defined name

#### INTERFACELIST
A list of interfaces the source detected it can support.  This is the result of running an interface scan or list.

Content:
Msgpack packed array containing mspack dictionaries of the following:
* "interface": String interface compatible with a source=interface definition
* "flags": String flags, aggregated as "flag1=foo,flag2=bar", compatible with a source=interface:flags source definition.

#### MESSAGE
MESSAGE KV pairs bridge directly to the messagebus of the Kismet server and are presented to users, logged, etc.  Message values may also be used in error reports if a source fails to open or a similar error occurs.

Content:

Msgpack packed dictionary containing the following values:
* "flags": uint32 message type flags (defined in `messagebus.h`)
* "msg": string, containing message content

#### PACKET
The PACKET KV pair contains a captured packet.  Datasources which operate on a packet level should use this to inject packets directly into the Kismet packetchain for decoding by a DLT handler.

A PACKET record is inserted into the Kismet packetchain packet as a "LINKFRAME" record.

Content:

Msgpack packed dictionary containing the following:
* "tv_sec": uint64 timestamp in seconds since the epoch (time_t)
* "tv_usec": uint64 timestamp in microseconds after the second
* "size": uint64 integer size of packet bytes
* "packet": binary/raw (interpreted as uint8[]) content of packet.  Size must match the size field.

#### SIGNAL
SIGNAL KV pairs can be added to data frames when the signal values are not included in the existing data.  For example, a driver reporting radiotap or PPI packets would not need to include a SIGNAL pair, however a driver decoding a SDR signal or other raw radio information could include it.

Whenever possible, signal levels should be reported in dBm, as RSSI values cannot be automatically scaled by the Kismet UI.

If a human-readable channel representation is not available due to the characteristics of the phy type, it should be presented as a frequency in a sensible format (such as "433.9MHz")

A SIGNAL record is inserted into the Kismet packetchain packet as a "RADIODATA" record.

Content:

Msgpack packed dictionary containing the following:
* "signal_dbm": int32 signal value in dBm (optional)
* "noise_dbm": int32 noise value in dBm (optional)
* "signal_rssi": int32 signal value in RSSI, dependent on device scaling factors (optional)
* "noise_rssi": int32 noise value in RSSI, dependent on device scaling factors (optional)
* "freq_khz": double-precision float of the center frequency of the signal record, in kHz
* "channel": arbitrary string representing a human-readable channel
* "datarate": double-precision float representing a phy-specific data rate (optional)

#### SOURCETYPE
A simple string value of the source type - this must match the definition in the datasource code for Kismet.  This value is used to tell Kismet what type of device a network based remote capture needs.

Simple string `(char *)` of the source type, length dictated by the KV length record.

Example:

`"linuxwifi"`

#### SPECSET
Sources which support raw spectrum capture should accept this KV in the CONFIGURE frame and return it in the PROBERESP and OPENRESP frames.  Modeled on the configuration required to configure the *_sweep tools (such as hackrf_sweep), SPECSET passes the basic set of spectrum configuration parameters.

Content:

Msgpack packed dictionary containing the following:
* "start_mhz": uint64 unsigned value, starting frequency, in MHz, of sweep (optional)
* "end_mhz": uint64 unsigned value, ending frequency, in MHz, of sweep (optional)
* "samples_per_freq": uint64 unsigned value, number of samples taken per frequency bin (optional)
* "bin_width": uint64 unsigned value, width of each sample bin, in Hz (optional)
* "amp": uint8 unsigned value, treated as boolean, enables amp (if available) (optional)
* "if_amp": uint64 unsigned value, LNA/IF amplifier level (optional)
* "baseband_amp": uint64 unsigned value, Baseband/VGA amplifier level (optional)

#### SPECTRUM
Sources which report raw spectrum should send it using this KV.  Modeled after the output format from the *_sweep tools (hackrf_sweep, rtl_sweep, etc), this allows for simple transmission of the spectrum data as dB levels.

A SPECTRUM record is inserted into the Kismet packetchain packet as a "SPECTRUM" record.  If a PACKET record is also found, both may be inserted into the same packet.

Content:

Msgpack packed dictionary containing the following:
* "timestamp": double timestamp in seconds+microseconds since the epoch
* "mhz_low": uint64 lowest frequency of sweep, in MHz
* "mhz_high": uint64 highest frequency of sweep, in MHz
* "hz_bin_width": uint64 width of each signal record, in Hz
* "db_samples": vector/array of samples, in dB, as int16 data.

Example:

{ "timestamp": 12345, "mhz_low": 2400000, "mhz_high": 2480000, "hz_bin_width": 1000000, "db_samples": [ -60, -60, -60, -10, -20, ... ] }

#### SUCCESS
A simple boolean indicating success or failure of the relevant command.  This value is padded to 4 bytes and is followed by the `uint32_t` sequence number of the command, if any, this success value applies to.

Content:
* A single byte (`uint8_t`) indicating success (non-zero) or failure (zero).
* Three bytes of padding to align word boundaries
* An unsigned 32 bit int (`uint32_t`) of the command sequence number this is acknowledging.

#### UUID
Capture-binary derived UUID (often based on the MAC address of the interface, if available).  Transmitted to the Kismet server for tracking, if the UUID is not already overridden by the source definition.

Content:

Simple string `(char *)` of the UUID, length dictated by the KV length record.

Example:

`"b4d6e78a-109a-11e7-a60d-09076f44c503"`

#### WARNING
A warning to the user about an unusual interface state, which is displayed whenever the interface details are shown and may be shown to the user in other ways as well.

This is a good way to report non-fatal but non-optimal conditions - for example the Linux Wi-Fi capture system uses this to alert the user to a problem with regulatory domains.

Content:

Simple string `(char *)` of the warning text, length indicated by the KV length record.

Example:

`"System-wide regulatory domain set to '00 - Unknown', this can cause problems with channel hopping."`

## Defining the driver:  Deriving from KisDatasource

The datasource driver is the C++ component which brokers interactions between the capture binary and Kismet.

All datasources are derived from `KisDatasource`.  A KisDatasource is based on a `tracker_component` to provide easy export of capture status.

The amount of customization required when writing a KisDatasource driver depends on the amount of custom data being passed over the IPC channel.  For packet-based data sources, there should be little additional customization required, however sources which pass complex pre-parsed objects will need to customize the protocol handling methods.

KisDatasource instances are used in two ways:
1. *Maintenance* instances are used as factories to create new instances.  A maintenance instance is used to enumerate supported capture types, initiate probes to find a type automatically, and to build a capture instance.
2. *Capture* instances are bound to an IPC process for the duration of capture and are used to process the full capture protocol.

At a minimum, new datasources must implement the following from KisDatasource:

*probe_type(...)* is called to find out if this datasource supports a known type.  A datasource should return `true` for each type name supported.
```C++
virtual bool probe_type(string in_type) {
    if (StrLower(in_type) == "customfoo")
        return true;

    return false;
}
```

*build_data_source()* is the factory method used for returning an instance of the KisDatasource.  A datasource should simply return a new instance of its custom type.
```C++
virtual KisDataSource *build_data_source() {
    return new CustomKisDataSource(globalreg);
}
```

A datasource which operates by passing packets should be able to function with no further customization:  Packet data passed via the `PACKET` record will be
decapsulated and inserted into the packetchain with the proper DLT.

## Handling the PHY

Kismet defines `PhyHandler` objects to handle different physical layer types - for example there are phyhandlers for IEEE802.11, Bluetooth, and so on.

A phy handler is responsible for defining any custom data structures specific to that phy, converting phy-specific data to the common interface so that Kismet can make generic devices for it, providing any additional javascript and web resources, and similar tasks.

## Defining the PHY

Phy handlers are derived from the base `Kis_Phy_Handler` class.

At a minumum a new phy must provide (and override):

* The basic C++ contructor and destructor implementations
* The create function to build an actual instance of the phy handler
* A common classifier stage to create common info from the custom packet info
* A storage loader function to attach any custom data when a device is loaded from storage

## Loading from storage and custom data types

A new phy will almost certainly define a custom tracked data type - `dot11_tracked_device` and `bluetooth_tracked_device` for instance.  As part of defining this custom type, the phy must provide a storage loader function to import stored data into a proper object.

In addition, there are some specific pitfalls when loading custom objects - be sure to check the  "Restoring vector and map objects" section of of the `tracked_component` docs!

## Handling the DLT

A datasource which is packet-based but does not conform to an existing DLT defined in Kismet will often need to provide its own DLT handler.

### Do I need a custom DLT handler?

If data records are entirely parsed by the classifier (see below for more information), then a separate DLT handler may not be necessary, however if your DLT embeds signal, location, or other information which needs to be made available to other Kismet data handlers, it should be decoded by your DLT handler.

Capture sources implementing alternate capture methods for known DLTs (for instance, support for 802.11 on other operating systems, etc) do not need to implement a new DLT handler.

### Deriving the DLT

Kismet DLT handlers are derived from `Kis_DLT_Handler` from `kis_dlt.h`.  A DLT handler needs to override the constructor and the `HandlePacket(...)` functions:

```C++
class DLT_Example : public Kis_DLT_Handler {
public:
    DLT_Example(GlobalRegistry *in_globalreg);

    virtual int HandlePacket(kis_packet *in_pack);
};

DLT_Example::DLT_Example(GlobalRegistry *in_globalreg) :
    Kis_DLT_Handler(in_globalreg) {

    /* Packet components and insertion into the packetchain is handled
       automatically by the Kis_DLT_Handler constructor.  All that needs
       to happen here is setting the name and DLT type */
    dlt_name = "Example DLT";

    /* DLT type is set in tcpdump.h */
    dlt = DLT_SOME_EXAMPLE;

    /* Optionally, announce that we're loaded */
    _MSG("Registering support for DLT_SOME_EXAMPLE", MSGFLAG_INFO);
}

/* HandlePacket(...) is called by the packet chain with the packet data
   as reported by the datasource.  This may already include GPS and signal
   information, as well as the actual link data frame.

   HandlePacket is responsible for decapsulating the DLT, creating any
   additional kis_packet records, and prepping the data for the classifier
   stage.
*/

int DLT_Example::HandlePacket(kis_packet *in_pack) {
    /* Example sanity check - do we already have packet data
       decapsulated?  For a type like radiotap or PPI that encodes another
       DLT, this encapsulated chunk might be handled differently */
    kis_datachunk *decapchunk =
        (kis_datachunk *) in_pack->fetch(pack_comp_decap);
    if (decapchunk != NULL) {
        return 1;
    }

    /* Get the linklayer data record */
    kis_datachunk *linkdata =
        (kis_datachunk *) in_pack->fetch(pack_comp_linkframe);

    /* Sanity check - do we even have a link chunk? */
    if (linkdata == NULL) {
        return 1;
    }

    /* Sanity check - does the DLT match? */
    if (linkdata->dlt != dlt) {
        return 1;
    }

    /* Other code goes here */
}

```

## Handling Non-Packet Data

Non-packet data can be decapsulated by extending the `KisDataSource::handle_packet` method.  By default this method handles defined packet types, an extended version should first call the parent instance.

```C++
void SomeDataSource::handle_packet(string in_type, KVmap in_kvmap) {
    KisDataSource::handle_packet(in_type, in_kvmap);

    string ltype = StrLower(in_type);

    if (ltype == "customtype") {
        handle_packet_custom(in_kvmap);
    }
}
```

Extended information can be added to a packet as a custom record and transmitted via the Kismet packetchain, or can be injected directly into the tracker for the new phy type (See the [datatracker](/docs/dev/datatracker.html) docs for more information).  Injecting into the packet chain allows existing Kismet code to track signal levels, location, etc, automatically.

If the incoming data is directly injected into the data tracking system for the new phy type, then special care must be taken to create pseudo-packet records for the core device tracking system.  Ultimately, a pseudo-packet event must be created, either when processing the custom IPC packet or in the device classifier.  Generally, it is recommended that a datasource attach the custom record to a packet object and process it via the packetchain as documented in [datatracker](/docs/dev/datatracker.html).

When processing a custom frame, existing KV pair handlers can be used.  For example:

```C++
void SomeDataSource::handle_packet_custom(KVmap in_kvpairs) {
    KVmap::iterator i;

    // We inject into the packetchain so we need to make a packet
    kis_packet *packet = NULL;

    // We accept signal and gps info in our custom IPC packet
    kis_layer1_packinfo *siginfo = NULL;
    kis_gps_packinfo *gpsinfo = NULL;

    // We accept messages, so process them using the existin message KV
    // handler
    if ((i = in_kvpairs.find("message")) != in_kvpairs.end()) {
        handle_kv_message(i->second);
    }

    // Generate a packet using the packetchain
    packet = packetchain->GeneratePacket();

    // Gather signal data if we have any
    if ((i = in_kvpairs.find("signal")) != in_kvpairs.end()) {
        siginfo = handle_kv_signal(i->second);
    }

    // Gather GPS data if we have any
    if ((i = in_kvpairs.find("gps")) != in_kvpairs.end()) {
        gpsinfo = handle_kv_gps(i->second);
    }

    // Add them to the packet
    if (siginfo != NULL) {
        packet->insert(pack_comp_l1info, siginfo);
    }

    if (gpsinfo != NULL) {
        packet->insert(pack_comp_gps, gpsinfo);
    }

    // Gather whatever custom data we have and add it to the packet
    if ((i = in_kvpairs.find("customfoo")) != in_kvpairs.end()) {
        handle_kv_customfoo(i->second, packet);
    }

    // Update the last valid report time
    inc_num_reports(1);
    set_last_report_time(globalreg->timestamp.tv_sec);

    // Inject the packet into the packet chain, this will clean up
    // the packet when it's done with it automatically.
    packetchain->ProcessPacket(packet);
}

```
