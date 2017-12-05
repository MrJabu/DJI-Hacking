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

#ifndef __TIMETRACKER_H__
#define __TIMETRACKER_H__

#include "config.h"

#include <stdio.h>
#include <time.h>
#include <list>
#include <map>
#include <vector>
#include <algorithm>
#include <string>

#include <functional>

#include "globalregistry.h"
#include "kis_mutex.h"

// For ubertooth and a few older plugins that compile against both svn and old
#define KIS_NEW_TIMER_PARM	1

#define TIMEEVENT_PARMS Timetracker::timer_event *evt __attribute__ ((unused)), \
    void *auxptr __attribute__ ((unused)), GlobalRegistry *globalreg __attribute__ ((unused))

class TimetrackerEvent;

class Timetracker : public LifetimeGlobal {
public:
    struct timer_event {
        int timer_id;

        // Time it was scheduled
        struct timeval schedule_tm;

        // Explicit trigger time or number of 100000us timeslices
        struct timeval trigger_tm;
        int timeslices;

        // Event is rescheduled again once it expires, if it's a timesliced event
        int recurring;

        // Event, if we were passed a class
        TimetrackerEvent *event;

        // Function if we were passed a lambda
        std::function<int (int)> event_func;

        // C function, if we weren't
        int (*callback)(timer_event *, void *, GlobalRegistry *);
        void *callback_parm;
    };

    // Sort alerts by alert trigger time
    class SortTimerEventsTrigger {
    public:
        inline bool operator() (const Timetracker::timer_event *x, 
								const Timetracker::timer_event *y) const {
            if ((x->trigger_tm.tv_sec < y->trigger_tm.tv_sec) ||
                ((x->trigger_tm.tv_sec == y->trigger_tm.tv_sec) && 
				 (x->trigger_tm.tv_usec < y->trigger_tm.tv_usec)))
                return 1;

            return 0;
        }
    };

    static shared_ptr<Timetracker> create_timetracker(GlobalRegistry *in_globalreg) {
        shared_ptr<Timetracker> mon(new Timetracker(in_globalreg));
        in_globalreg->timetracker = mon.get();
        in_globalreg->RegisterLifetimeGlobal(mon);
        in_globalreg->InsertGlobal("TIMETRACKER", mon);
        return mon;
    }
private:
    Timetracker(GlobalRegistry *in_globalreg);

public:
    virtual ~Timetracker();

    // Tick and handle timers
    int Tick();

    // Register an optionally recurring timer.  Slices are 1/100th of a second,
    // the smallest linux can slice without getting into weird calls.
    int RegisterTimer(int in_timeslices, struct timeval *in_trigger,
                      int in_recurring, 
                      int (*in_callback)(timer_event *, void *, GlobalRegistry *),
                      void *in_parm);

    int RegisterTimer(int timeslices, struct timeval *in_trigger,
            int in_recurring, TimetrackerEvent *event);

    int RegisterTimer(int timeslices, struct timeval *in_trigger,
            int in_recurring, std::function<int (int)> event);

    // Remove a timer that's going to execute
    int RemoveTimer(int timer_id);

protected:
    GlobalRegistry *globalreg;

    kis_recursive_timed_mutex time_mutex;

    // Nonblocking versions
    int RegisterTimer_nb(int in_timeslices, struct timeval *in_trigger,
                      int in_recurring, 
                      int (*in_callback)(timer_event *, void *, GlobalRegistry *),
                      void *in_parm);
    int RegisterTimer_nb(int timeslices, struct timeval *in_trigger,
            int in_recurring, TimetrackerEvent *event);
    int RegisterTimer_nb(int timeslices, struct timeval *in_trigger,
            int in_recurring, std::function<int (int)> event);
    int RemoveTimer_nb(int timer_id);

    int next_timer_id;
    map<int, timer_event *> timer_map;
    vector<timer_event *> sorted_timers;
};

class TimetrackerEvent {
public:
    // Called when event triggers
    virtual int timetracker_event(int event_id __attribute__ ((unused))) { return 0; };

protected:
    int timer_id;

};

#endif
