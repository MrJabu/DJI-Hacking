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

#ifndef __KIS_DLT_PPI_H__
#define __KIS_DLT_PPI_H__

#include "config.h"

#include "globalregistry.h"
#include "packet.h"
#include "packetchain.h"
#include "kis_dlt.h"

class Kis_DLT_PPI : public Kis_DLT_Handler {
public:
	Kis_DLT_PPI() { fprintf(stderr, "FATAL OOPS: Kis_DLT_PPI()\n"); exit(1); }
	Kis_DLT_PPI(GlobalRegistry *in_globalreg);

	virtual int HandlePacket(kis_packet *in_pack);

	~Kis_DLT_PPI();
};

#endif

