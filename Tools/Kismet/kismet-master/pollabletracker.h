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

#ifndef __POLLABLETRACKER_H__
#define __POLLABLETRACKER_H__

#include "config.h"

#include <vector>

#include "kis_mutex.h"
#include "pollable.h"
#include "globalregistry.h"

/* Pollable subsystem tracker
 *
 * Monitors pollable events and wraps them into a single select() loop
 * and handles erroring out sources after their events have been processed.
 *
 * Add/remove from the pollable vector is handled asynchronously to protect the
 * integrity of the pollable object itself and the internal pollable vectors;
 * adds and removes are synced at the next descriptor or poll event.
 */

class PollableTracker : public LifetimeGlobal {
public:
    static shared_ptr<PollableTracker> 
        create_pollabletracker(GlobalRegistry *in_globalreg) {
        shared_ptr<PollableTracker> mon(new PollableTracker(in_globalreg));
        in_globalreg->RegisterLifetimeGlobal(mon);
        in_globalreg->InsertGlobal("POLLABLETRACKER", mon);
        return mon;
    }

private:
    PollableTracker(GlobalRegistry *in_globalreg);

public:
    virtual ~PollableTracker();

    // Add a pollable item
    void RegisterPollable(shared_ptr<Pollable> in_pollable);

    // Schedule a pollable item to be removed as soon as the current
    // operation completes (or the next one begins); This allows errored sources
    // to remove themselves once their tasks are complete.
    void RemovePollable(shared_ptr<Pollable> in_pollable);

    // populate the FD sets for polling, populates rset and wset
    //
    // returns:
    // 0+   Maxmimum FD to be passed to select()
    // -1   Error
    int MergePollableFds(fd_set *rset, fd_set *wset);
   
    // Poll each item in a set
    //
    // returns:
    // 0+   Number of pollable items processed
    // -1   Error
    int ProcessPollableSelect(fd_set rset, fd_set wset);

protected:
    GlobalRegistry *globalreg;

    kis_recursive_timed_mutex pollable_mutex;

    vector<shared_ptr<Pollable> > pollable_vec;
    vector<shared_ptr<Pollable> > add_vec;
    vector<shared_ptr<Pollable> > remove_vec;

    void Maintenance();
};

#endif
