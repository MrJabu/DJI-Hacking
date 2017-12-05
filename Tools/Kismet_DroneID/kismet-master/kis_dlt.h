/*
    This file is part of Kismet

    Kismet is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Kismet is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Kismet; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

/* DLT handler framework */

#ifndef __KIS_DLT_H__
#define __KIS_DLT_H__

#include "config.h"

#include "globalregistry.h"
#include "packet.h"
#include "packetchain.h"

class Kis_DLT_Handler : public SharedGlobalData {
public:
	Kis_DLT_Handler() { fprintf(stderr, "FATAL OOPS: Kis_DLT_Handler()\n"); exit(1); }
	Kis_DLT_Handler(GlobalRegistry *in_globalreg);

	virtual int HandlePacket(kis_packet *in_pack) = 0;

	~Kis_DLT_Handler();

	virtual int FetchDLT() { return dlt; }
	virtual string FetchDLTName() { return dlt_name; }

protected:
	GlobalRegistry *globalreg;
	string dlt_name;
	int dlt;
	int chainid;
	int pack_comp_linkframe, pack_comp_decap, pack_comp_datasrc,
		pack_comp_radiodata, pack_comp_gps, pack_comp_checksum;
};

#endif

