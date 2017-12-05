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

#ifndef __PIPECLIENT_H__
#define __PIPECLIENT_H__

#include "config.h"

#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

#include "kis_mutex.h"
#include "messagebus.h"
#include "globalregistry.h"
#include "buffer_handler.h"
#include "pollable.h"

// Pipe client code for communicating with another process
//
// Handles r/w against two pipe(2) pairs which should be provided by 
// the IPC spawning system.
//
// Populates the read buffer of a rbhandler and drains the write buffer
//
// Like other backend clients of a ringbuf handler, does not register as a read
// or write directly but consumes out of the handler
class PipeClient : public Pollable {
public:
    PipeClient(GlobalRegistry *in_globalreg, shared_ptr<BufferHandlerGeneric> in_rbhandler);
    virtual ~PipeClient();

    // Bind to a r/w pair of pipes
    int OpenPipes(int rpipe, int wpipe);
    void ClosePipes();

    // Pollable interface
    virtual int MergeSet(int in_max_fd, fd_set *out_rset, fd_set *out_wset);
    virtual int Poll(fd_set& in_rset, fd_set& in_wset);

    bool FetchConnected();

protected:
    kis_recursive_timed_mutex pipe_lock;

    GlobalRegistry *globalreg;
    shared_ptr<BufferHandlerGeneric> handler;

    int read_fd, write_fd;
};

#endif
