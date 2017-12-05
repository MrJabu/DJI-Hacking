-- Create a new dissector
DJI_P3_PROTO = Proto ("dji_p3", "DJI_P3","dji p3 UART protocol")

local SRC_DEST = {  [0] = 'Invalid',
                    [1] = 'Camera',
                    [2] = 'Unknown',
                    [3] = 'Flight Controller',
                    [4] = 'Gimbal',
                    [5] = 'Central Board',
                    [6] = 'Unknown',
                    [7] = 'OSMO',
                    [8] = 'Unknown',
                    [9] = 'HW Button',
                    [10] = 'Unknown',
                    [11] = 'Battery',
                    [12] = 'ESC',
                    [13] = 'Unknown',
                    [14] = 'Unknown',
                    [15] = 'Unknown',
                    [16] = 'Unknown',
                    [17] = 'Unknown',
                    [18] = 'Unknown',
                    [19] = 'Unknown',
                    [20] = 'Unknown',
                    [21] = 'Unknown',
                    [22] = 'Unknown',
                    [23] = 'Unknown',
                    [24] = 'Unknown',
                    [25] = 'Unknown',
                    [26] = 'Unknown',
                    [27] = 'Unknown',
                    [28] = 'Unknown',
                    [29] = 'Unknown',
                    [30] = 'Unknown',
                    [31] = 'Unknown'   }

local ACK_POLL = {  [0]="RSP",[1]="CMD",[2]="CMD",[3]="????"}

local CMD_SET = {   [0] = 'General', 
                    [1] = 'Special',
                    [2] = 'Camera',
                    [3] = 'Flight Controller',
                    [4] = 'Gimbal',
                    [5] = 'Central Board',
                    [6] = 'RC',
                    [7] = 'WiFi',
                    [8] = 'DM386',
                    [9] = 'HD Link',
                    [10] = 'Mono/Binocular',
                    [11] = 'Simulator',
                    [12] = 'ESC',
                    [13] = 'Battery',
                    [14] = 'Data Rec',
                    [15] = 'RTK',
                    [16] = 'Automation' }

-- CMD name decode tables
local GENERAL_CMDS = {      [0x01] = 'Inquiry', 
                            [0x07] = 'TBD', 
                            [0x0B] = 'REBOOT',
                            [0x0C] = 'TBD',
                            [0x32] = 'TBD',
                            [0xF1] = 'OFDM Tx State'}

local SPECIAL_CMDS = {      [0x01] = 'App Cmd' }

local CAMERA_CMDS = {       [0x80] = 'TBD' }

local FLIGHT_CTRLER_CMDS = {[0x1C] = 'TBD',
                            [0x2A] = 'App Cmd',
                            [0x2F] = 'Set Alarm',
                            [0x30] = 'Get Alarm',
                            [0x31] = 'Set Home Point',   --AC/RC/APP
                            [0x39] = 'TBD',
                            [0x33] = 'Set User String',
                            [0x34] = 'Get User String',
                            [0x39] = 'TBD',
                            [0x3A] = 'TBD',
                            [0x3B] = 'Set RC Lost Action',
                            [0x3C] = 'Get RC Lost Action',
                            [0x3D] = 'Set Timezone',
                            [0x3F] = 'TBD',     --Data transfer
                            [0x41] = 'TBD',     
                            [0x46] = 'TBD',
                            [0x47] = 'Toggle Whitelist',
                            [0x51] = 'TBD',
                            [0x52] = 'TBD',
                            [0x53] = 'TBD',
                            [0x60] = 'SVO API Transfer',
                            [0x62] = 'TBD',
                            [0x64] = 'TBD',     
                            [0x70] = 'TBD',     --Some licensing string check
                            [0x71] = 'TBD',     --Some licensing string check
                            [0x74] = 'Get Serial Number',  
                            [0x75] = 'Write EEPROM FC0',  
                            [0x76] = 'Read EEPROM FC0',
                            [0x80] = 'TBD',
                            [0x82] = 'Set Mission Length',
                            [0x83] = 'TBD',
                            [0x84] = 'TBD',
                            [0x85] = 'TBD',
                            [0x86] = 'WP Mission go/stop',
                            [0x87] = 'WP Mission pasue/resume',
                            [0x8A] = 'TBD',
                            [0x8B] = 'TBD',
                            [0x8C] = 'HP Mission pasue/resume',
                            [0x8D] = 'TBD',
                            [0x8E] = 'TBD',
                            [0x90] = 'TBD',
                            [0x91] = 'App Request Follow Mission',
                            [0x92] = 'Follow Mission pasue/resume',
                            [0x93] = 'TBD',
                            [0x96] = 'TBD',
                            [0x97] = 'TBD',
                            [0x98] = 'TBD',
                            [0x99] = 'TBD',
                            [0x9A] = 'TBD',
                            [0x9B] = 'TBD',
                            [0x9C] = 'Set WP Mission Idle V',
                            [0x9D] = 'Get WP Mission Idle V',

                            [0xAA] = 'TBD',
                            [0xAB] = 'Set Attitude',
                            [0xAC] = 'Set Tail Lock',
                            [0xF0] = 'TBD',
                            [0xF1] = 'TBD',
                            [0xF2] = 'TBD',
                            [0xF3] = 'TBD',
                            [0xF4] = 'TBD',
                            [0xF7] = 'TBD',
                            [0xF8] = 'TBD',
                            [0xF9] = 'TBD',
                            [0xFA] = 'Reset Flyc Params',
                            [0xFC] = 'TBD',
                            [0xFD] = 'TBD' }

local GIMBAL_CMDS = {       [0x05] = 'TBD',
                            [0x1C] = 'Set Gimbal type' }

local CENTRAL_BRD_CMDS = {  }

local RC_CMDS = {           [0x1C] = 'TBD' }

local WIFI_CMDS = {         [0x0E] = 'Get PSK',
                            [0x11] = 'TBD',
                            [0x1E] = 'Get SSID' }

local DM386_CMDS = {  }
local HD_LINK_CMDS = {  }
local MBINO_CMDS = {  }
local SIM_CMDS = {  }
local ESC_CMDS = {  }
local BATTERY_CMDS = {  }
local DATA_REC_CMDS = {  }
local RTK_CMDS = {  }
local AUTO_CMDS = {  }

local CMD_CMD_SET = {   [0] = GENERAL_CMDS,
                        [1] = SPECIAL_CMDS,
                        [2] = CAMERA_CMDS,
                        [3] = FLIGHT_CTRLER_CMDS,
                        [4] = GIMBAL_CMDS,
                        [5] = CENTRAL_BRD_CMDS,
                        [6] = RC_CMDS,
                        [7] = WIFI_CMDS,
                        [8] = DM386_CMDS,
                        [9] = HD_LINK_CMDS,
                        [10] = MBINO_CMDS,
                        [11] = SIM_CMDS,
                        [12] = ESC_CMDS,
                        [13] = BATTERY_CMDS,
                        [14] = DATA_REC_CMDS,
                        [15] = RTK_CMDS,
                        [16] = AUTO_CMDS    }

local f = DJI_P3_PROTO.fields
-- [0]  Start of Pkt, always 0x55
f.delimiter = ProtoField.uint8 ("dji_p3.delimiter", "Delimiter", base.HEX)
-- [1]  Length of Pkt 
f.length = ProtoField.uint16 ("dji_p3.length", "Length", base.HEX, nil, 0x3FF)
-- [2]  Protocol version
f.protocol_version = ProtoField.uint16 ("dji_p3.protover", "Protocol Version", base.HEX, nil, 0xFC00)
-- [3]  Data Type
f.datatype = ProtoField.uint8 ("dji_p3.hdr_crc", "Header CRC", base.HEX)
-- [4]  Sender
f.sender = ProtoField.uint8 ("dji_p3.sender", "Sender", base.DEC, SRC_DEST, 0x1F)
-- [5]  Receiver
f.receiver = ProtoField.uint8 ("dji_p3.receiver", "Receiver", base.DEC, SRC_DEST, 0x1F)
-- [6-7]  Sequence Ctr
f.seqctr = ProtoField.uint16 ("dji_p3.seqctr", "Seq Counter", base.DEC)
-- [8] Ack (to be improved)
f.ack = ProtoField.uint8 ("dji_p3.ack", "Ack", base.HEX, ACK_POLL, 0x60)
-- [9] Cmd Set
f.cmdset = ProtoField.uint8('dji_p3.cmdset', 'Cmd Set', base.DEC, CMD_SET, 0xFF)

-- [A] Cmd
f.cmd       = ProtoField.uint8('dji_p3.cmd',      'Cmd',          base.HEX)

-- [B] Payload (optional)
f.payload = ProtoField.bytes ("dji_p3.payload", "Payload", base.HEX)

-- [B+Payload] CRC
f.crc = ProtoField.uint16 ("dji_p3.crc", "CRC", base.HEX)

function set_info(cmd, pinfo, decodes)
    pinfo.cols.info = ""
    if decodes[cmd] == nil then
        pinfo.cols.info:append(string.format("Unknown [0x%02X]", cmd))
    else
        pinfo.cols.info:append(decodes[cmd])
    end
end

function main_dissector(buffer, pinfo, subtree)
    local offset = 1

    -- [1-2] The Pkt length | protocol version?
    local pkt_length = buffer(offset,2):le_uint()
    -- bit32 lib requires LUA 5.2
    pkt_length = bit32.band(pkt_length,  0x03FF)

    local pkt_length = buffer(offset,1):uint()

    subtree:add_le (f.length, buffer(offset, 2))
    subtree:add_le (f.protocol_version, buffer(offset, 2))
    offset = offset + 2

    -- [3] Header Checksum
    subtree:add (f.datatype, buffer(offset, 1))
    offset = offset + 1

    -- [4] Sender
    subtree:add (f.sender, buffer(offset, 1))
    offset = offset + 1

    -- [5] Receiver
    subtree:add (f.receiver, buffer(offset, 1))
    offset = offset + 1

    -- [6] Sequence Counter
    subtree:add_le (f.seqctr, buffer(offset, 2))
    offset = offset + 2

    -- [8] Ack
    subtree:add (f.ack, buffer(offset, 1))
    offset = offset + 1

    -- [9] Cmd Set
    local cmdset = buffer(offset,1):uint()
    subtree:add (f.cmdset, buffer(offset, 1))
    offset = offset + 1

    set_info(buffer(offset,1):uint(), pinfo, CMD_CMD_SET[cmdset])

    -- [A] Cmd
    subtree:add (f.cmd, buffer(offset, 1))
    offset = offset + 1

    -- [B] Payload    
    if pkt_length > 13 then
        subtree:add(f.payload, buffer(offset, pkt_length - 13))
    end

    -- CRC
    subtree:add_le(f.crc, buffer(pkt_length - 2, 2))
    offset = offset + 2
end



-- Top level battery dissector
local BATTERY_CMDS = {      [0x21] = 'Get Battery Type',
                                                [0x22] = 'Get Battery Status',
                                                [0x23] = 'DRM ?',
                                                [0x24] = 'DRM ?',
                                                [0x25] = 'Get Battery Warning Flags',
                                                [0x31] = 'Get Battery Settings',
                                                [0x34] = 'Unknown',
                                                [0x38] = 'Get Battery Barcode'  }


-- [1]  Length of Pkt 
f.batt_length = ProtoField.uint16 ("dji_p3.batt_length", "Length", base.HEX, nil, 0x3FF)
-- [2]  
f.batt_protocol_version = ProtoField.uint16 ("dji_p3.batt_protover", "Protocol Version", base.HEX, nil, 0xFC00)
-- [3] Cmd ID
f.batt_cmd = ProtoField.uint8('dji_p3.batt_cmd', 'Battery Cmd', base.HEX, BATTERY_CMDS)

f.batt_payload = ProtoField.bytes ("dji_p3.batt_payload", "Payload", base.HEX)

f.batt_crc = ProtoField.uint8 ("dji_p3.batt_crc", "CRC", base.HEX)

-- Battery Info 
f.batt_total_capacity = ProtoField.uint16 ("dji_p3.batt_total_capacity", "Total Cap", base.DEC)
f.batt_discharge_count = ProtoField.uint16 ("dji_p3.batt_discharge_count", "Discharge Count", base.DEC)
f.batt_nominal_capacity = ProtoField.uint16 ("dji_p3.batt_nominal_capacity", "Nominal Cap", base.DEC)
f.batt_nominal_voltage = ProtoField.uint16 ("dji_p3.batt_nominal_voltage", "Nominal V", base.DEC)
f.batt_battery_spec = ProtoField.uint16 ("dji_p3.batt_battery_spec", "Battery Spec", base.DEC)
f.batt_mfr_date = ProtoField.string ("dji_p3.batt_mfr_date", "Mfr Date")
f.batt_serial_number = ProtoField.uint16 ("dji_p3.batt_serial_number", "Serial Number", base.HEX)
f.batt_name = ProtoField.string("dji_p3.batt_name", "Name")
f.batt_unk1 = ProtoField.uint16 ("dji_p3.batt_unknown1", "Unknown", base.HEX)
f.batt_unk2 = ProtoField.uint16 ("dji_p3.batt_unknown2", "Unknown", base.HEX)
f.batt_fw_version = ProtoField.string ("dji_p3.batt_fw_version", "FW Version", base.HEX)
f.batt_life = ProtoField.uint16 ("dji_p3.batt_life", "Battery Life", base.DEC)

-- Battery Status
f.batt_temp = ProtoField.uint16 ("dji_p3.batt_temp", "Temp", base.DEC)
f.batt_total_v = ProtoField.uint16 ("dji_p3.batt_total_v", "Total mV", base.DEC)
f.batt_current = ProtoField.int32 ("dji_p3.batt_current", "Current", base.DEC)
f.batt_ave_current = ProtoField.int32 ("dji_p3.batt_ave_current", "Average Current", base.DEC)
f.batt_state_of_charge = ProtoField.uint16 ("dji_p3.batt_charge", "Charge (%)", base.DEC)
f.batt_cell_v = ProtoField.uint16 ("dji_p3.batt_cell_v", "Cell mV", base.DEC)
f.batt_cell_eol = ProtoField.uint16 ("dji_p3.batt_cell_eol", "EOL", base.HEX)
f.batt_power_status = ProtoField.string("dji_p3.batt_status", "Power Status")

--Battery barcode
f.batt_barcode = ProtoField.bytes("dji_p3.batt_barcode", "Barcode", base.DEC)

function battery_barcode_dissector(batt_length, buffer, pinfo, subtree)
    local offset = 5
    if batt_length ~= 14 then
        subtree:add(f.batt_barcode, buffer(offset, (batt_length -6)))

    end
end

function battery_status_dissector(batt_length, buffer, pinfo, subtree)
    local offset = 5
--    local cell 
    if batt_length ~= 14 then
        subtree:add_le (f.batt_temp, buffer(offset, 2))
        offset = offset + 2
        subtree:add_le (f.batt_total_v, buffer(offset, 2))
        offset = offset + 2
        subtree:add_le (f.batt_current, buffer(offset, 2))
        offset = offset + 4
        subtree:add_le (f.batt_ave_current, buffer(offset, 2))
        offset = offset + 4
        subtree:add_le (f.batt_state_of_charge, buffer(offset, 2))
        offset = offset + 2

        for cell = 1, 5 do
            subtree:add_le (f.batt_cell_v, buffer(offset, 2))
            offset = offset + 2
        end
        
        subtree:add_le (f.batt_cell_eol, buffer(offset, 2))
        offset = offset + 2

        local pstatus = buffer(offset,1):uint()
        if pstatus == 0x00 then
            subtree:add(f.batt_power_status, "On")
        elseif pstatus == 0xFF then
            subtree:add(f.batt_power_status, "Powering Off")
        else
            subtree:add(f.batt_power_status, "Unknown")
        end
        
    end

end

function battery_type_dissector(batt_length, buffer, pinfo, subtree)
    local offset = 5

    if batt_length ~= 14 then
        subtree:add_le (f.batt_total_capacity, buffer(offset, 2))
        offset = offset + 2
        subtree:add_le (f.batt_discharge_count, buffer(offset, 2))
        offset = offset + 2
        subtree:add_le (f.batt_nominal_capacity, buffer(offset, 2))
        offset = offset + 2
        subtree:add_le (f.batt_nominal_voltage, buffer(offset, 2))
        offset = offset + 2
        subtree:add_le (f.batt_battery_spec, buffer(offset, 2))
        offset = offset + 2

        local md = buffer(offset, 2):le_uint()
        local year = bit32.arshift(md,9) + 1980
        local month = bit32.band(bit32.arshift(md,5), 0x0F)
        local day = bit32.band(md, 0x01F)
        local date = string.format("%02d/%02d/%04d", day, month, year)
        subtree:add(f.batt_mfr_date, date)
        offset = offset + 2

        subtree:add_le (f.batt_serial_number, buffer(offset, 2))
        offset = offset + 2

        subtree:add(f.batt_name, buffer(offset, 15))
        offset = offset + 15

        subtree:add_le (f.batt_unk1, buffer(offset, 2))
        offset = offset + 2

        subtree:add_le (f.batt_unk2, buffer(offset, 2))
        offset = offset + 2
        
        local version = string.format("%02d.%02d.%02d.%02d", buffer(offset,1):uint(), buffer(offset+1,1):uint(), buffer(offset+2,1):uint(), buffer(offset+3,1):uint())
        subtree:add(f.batt_fw_version, version)
        offset = offset + 4

        subtree:add_le (f.batt_life, buffer(offset, 2))
        offset = offset + 2

    end
end

function battery_dissector(buffer, pinfo, subtree)
    local offset = 1

    local batt_length = buffer(offset,2):le_uint()
    -- bit32 lib requires LUA 5.2
    batt_length = bit32.band(batt_length,  0x03FF)
    
    subtree:add_le (f.batt_length, buffer(offset, 2))
    subtree:add_le (f.batt_protocol_version, buffer(offset, 2))
    offset = offset + 2

    batt_cmd = buffer(offset, 1):uint()
    subtree:add (f.batt_cmd, buffer(offset, 1))
    offset = offset + 1

    if batt_length > 5 then
        payload_tree = subtree:add(f.batt_payload, buffer(4, batt_length - 5))

        if batt_cmd == 0x21 then
            battery_type_dissector(batt_length, buffer, pinfo, payload_tree)
        elseif batt_cmd == 0x22 then
            battery_status_dissector(batt_length, buffer, pinfo, payload_tree)

        elseif batt_cmd == 0x38 then
            battery_barcode_dissector(batt_length, buffer, pinfo, payload_tree)
        else

        end

    end 

    subtree:add(f.batt_crc, buffer(batt_length - 1, 1))
end

-- The protocol dissector itself
function DJI_P3_PROTO.dissector (buffer, pinfo, tree)
    local subtree = tree:add (DJI_P3_PROTO, buffer())

    -- The Pkt start byte (0x55 or 0xAB determines the pkt type)
    local offset = 0

    local pkt_type = buffer(offset,1):uint()
    subtree:add (f.delimiter, buffer(offset, 1))
    offset = offset + 1

    if pkt_type == 0xAB then
        battery_dissector(buffer, pinfo, subtree)
    else
        main_dissector(buffer, pinfo, subtree)
    end
    
end

-- A initialization routine
local packet_counter

function DJI_P3_PROTO.init ()
    packet_counter = 0
end

