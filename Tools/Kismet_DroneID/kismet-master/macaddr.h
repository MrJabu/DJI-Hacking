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

#ifndef __MACADDR_H__
#define __MACADDR_H__

#include "config.h"

#include <stdio.h>
#include <ctype.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <string.h>
#include <signal.h>
#include <unistd.h>
#ifdef HAVE_STDINT_H
#include <stdint.h>
#endif
#ifdef HAVE_INTTYPES_H
#include <inttypes.h>
#endif
#include <algorithm>
#include <string>
#include <vector>
#include <map>
#include <sstream>
#include <iomanip>

// Maximum of 6 octets in a "mac" we handle
#define MAC_LEN_MAX		6

// Mac address transformed into 64bit int for fast sorting
//
// Supports mac addresses up to 7 octets, with optional
// masking, IP-subnet style.
//
struct mac_addr {
    uint64_t longmac;
    uint64_t longmask;
    int error;

    // Convert a string mac address to the long-int storage format, with 
	// mask conversion if present.
    void string2long(const char *in) {
        error = 0;

        longmac = 0;

        longmask = (uint64_t) -1;

		short unsigned int byte;

		int nbyte = 0;
		int mode = 0;

		while (*in) {
			if (in[0] == ':') {
				in++;
				continue;
			}

			if (in[0] == '/' || in[0] == '*') {
                longmask = 0L;
				mode = 1;
				nbyte = 0;
				in++;
				continue;
			}

			if (sscanf(in, "%2hX", &byte) != 1) {
                // printf("couldn't read byte in pos %d '%s' %x\n", nbyte, in, in[0]);
				error = 1;
				break;
			}

			if (strlen(in) >= 2)
				in += 2;
			else
				in++;

			if (nbyte >= MAC_LEN_MAX) {
                // printf("pos > max len\n");
				error = 1;
				break;
			}

			if (mode == 0) {
				longmac |= (uint64_t) byte << ((MAC_LEN_MAX - nbyte - 1) * 8);
			} else if (mode == 1) {
				longmask |= (uint64_t) byte << ((MAC_LEN_MAX - nbyte - 1) * 8);
            }

			nbyte++;
		}
    }

    inline mac_addr() {
        longmac = 0;
        longmask = (uint64_t) -1;
        error = 0;
    }

    inline mac_addr(const char *in) {
        string2long(in);
    }

	inline mac_addr(const string in) {
		string2long(in.c_str());
	}

    inline mac_addr(int in __attribute__((unused))) {
        longmac = 0;
        longmask = (uint64_t) -1;
        error = 0;
    } 

    inline mac_addr(uint8_t *in, unsigned int len) {
        error = 0;
        longmac = 0;
        longmask = (uint64_t) -1;

        for (unsigned int x = 0; x < len && x < MAC_LEN_MAX; x++) {
            longmac |= (uint64_t) in[x] << ((MAC_LEN_MAX - x - 1) * 8);
        }
    }

    inline mac_addr(uint8_t *in, unsigned int len, unsigned int mask) {
        error = 0;
        longmac = 0;
        longmask = (uint64_t) -1;

        for (unsigned int x = 0; x < len && x < MAC_LEN_MAX; x++) {
            longmac |= (uint64_t) in[x] << ((MAC_LEN_MAX - x - 1) * 8);
        }

        longmask = (longmask >> (64 - mask)) << mask;
    }

    // Convert a string to a positional search fragment, places fragent
    // in ret_term and length of fragment in ret_len
    inline static bool PrepareSearchTerm(string s, uint64_t &ret_term, 
            unsigned int &ret_len) {

		short unsigned int byte;
		int nbyte = 0;
        const char *in = s.c_str();

        uint64_t temp_long = 0LL;

        ret_term = 0LL;

        // Parse the same way as we parse a string into a mac, count the number 
        // of bytes we found
		while (*in) {
			if (in[0] == ':') {
				in++;
				continue;
			}

			if (sscanf(in, "%2hX", &byte) != 1) {
                ret_len = 0;
                return false;
			}

			if (strlen(in) >= 2)
				in += 2;
			else
				in++;

			if (nbyte >= MAC_LEN_MAX) {
                ret_len = 0;
                return false;
			}

            temp_long |= (uint64_t) byte << ((MAC_LEN_MAX - nbyte - 1) * 8);

			nbyte++;
		}

        ret_len = nbyte;
        ret_term = temp_long >> ((MAC_LEN_MAX - nbyte) * 8);

        return true;
    }

    // Match against a partial MAC address, prepared with PrepareSearchTerm
    inline bool PartialSearch(uint64_t in_term, unsigned int in_len) {
        unsigned char *rt = (uint8_t *) &in_term;
        unsigned char *rlm = (uint8_t *) &longmac;

        for (unsigned int p = 0; p <= MAC_LEN_MAX - in_len; p++) {
            if (memcmp(rt, rlm + p, in_len) == 0)
                return true;
        }

        return false;
    }

    // Masked MAC compare
    inline bool operator== (const mac_addr& op) const {
        if (longmask < op.longmask)
            return ((longmac & longmask) == (op.longmac & longmask));

        return ((longmac & op.longmask) == (op.longmac & op.longmask));
    }

	inline bool operator== (const unsigned long int op) const {
		return longmac == op;
	}

    // MAC compare
    inline bool operator!= (const mac_addr& op) const {
        if (longmask < op.longmask)
            return ((longmac & longmask) != (op.longmac & longmask));

        return ((longmac & op.longmask) != (op.longmac & op.longmask));
    }

    // mac less-than-eq
    inline bool operator<=(const mac_addr& op) const {
        return (longmac & op.longmask) == (op.longmac & op.longmask);
    }

    // MAC less-than for STL sorts...
    inline bool operator< (const mac_addr& op) const {
        return ((longmac & longmask) < (op.longmac & longmask));
    }

    mac_addr& operator= (const mac_addr& op) {
        longmac = op.longmac;
        longmask = op.longmask;
        error = op.error;
        return *this;
    }

    mac_addr& operator= (const char *in) {
        string2long(in);

        return *this;
    }

    mac_addr& operator++() {
        longmac++;
        return *this;
    }

    mac_addr operator++(int) {
        mac_addr tmp = *this;
        ++*this;
        return tmp;
    }

    inline unsigned int index64(uint64_t val, int index) const {
        // Bitshift kung-foo
        return (uint8_t) (val >> ((MAC_LEN_MAX - index - 1) * 8));
    }

    inline unsigned int operator[] (int index) const {
        int mdex = index;
        if (index < 0 || index >= MAC_LEN_MAX)
            mdex = 0;

        return index64(longmac, mdex);
    }

	// Return the top 3 of the mac. 
	inline uint32_t OUI() const {
		return (longmac >> (3 * 8)) & 0x00FFFFFF;
	}

    static inline uint32_t OUI(uint8_t *val) {
        return (val[0] << 16) | (val[1] << 8) | val[2];
    }

    static inline uint32_t OUI(short int *val) {
        return (val[0] << 16) | (val[1] << 8) | val[2];
    }

    inline string Mac2String() const {
		ostringstream osstr;

		for (unsigned int x = 0; x < MAC_LEN_MAX; x++) {
			osstr << hex << setw(2) << setfill('0') << uppercase << index64(longmac, x);
			if (x != MAC_LEN_MAX - 1)
				osstr << ':';
		}

		return osstr.str();
    }

    inline string MacMask2String() const {
		ostringstream osstr;

		for (unsigned int x = 0; x < MAC_LEN_MAX; x++) {
			osstr << hex << setw(2) << setfill('0') << uppercase << index64(longmask, x);
			if (x != MAC_LEN_MAX - 1)
				osstr << ':';
		}

		return osstr.str();
    }

    inline uint64_t GetAsLong() const {
        return longmac;
    }

    inline string MacFull2String() const {
        return Mac2String() + "/" + MacMask2String();
    }

};


// A templated container for storing groups of masked mac addresses.  A stl-map 
// will work for single macs, but we need this for smart mask matching on 
// more complex sets.  Iterators in this class only work as incremental, 
// because thats all I need right now.  This whole thing is really an ugly, 
// ugly kluge, and if I really had any need for it to be more extendible I'd 
// rewrite it to use std::iterator and other good stuff.  But, I don't,
// it works, and I need to move on to other areas.
template<class T>
class macmap {
protected:
    struct mask_vec_content {
        mac_addr mac;
        T value;
    };

    struct mask_vec_offsets {
        unsigned int first;
        unsigned int last;
    };

    class SortMaskVec {
    public:
        inline bool operator() (const macmap::mask_vec_content x, 
								const macmap::mask_vec_content y) const {
            return (x.mac < y.mac);
        }
    };

public:
    // This isn't quite like STL iterators, because I'm too damned lazy to deal 
	// with all the nasty STL hoop-jumping.  This does provide a somewhat-stl-ish 
	// interface to iterating through the singleton and masked maps
    class iterator {
        friend class macmap;

    public:
        inline iterator(macmap<T> *in_owner) {
            owner = in_owner;

            if (owner->singleton_map.size() > 0) {
                singleton_itr = owner->singleton_map.begin();
                vector_itr = -1;
                first = singleton_itr->first;
                second = &(singleton_itr->second);
            } else if (owner->mask_vec.size() > 0) {
                singleton_itr = owner->singleton_map.end();
                vector_itr = 0;
                first = owner->mask_vec[0].mac;
                second = &(owner->mask_vec[0].value);
            } else {
                singleton_itr = owner->singleton_map.end();
                vector_itr = owner->mask_vec.size();
				second = NULL;
            }
        }

        // Prefix
        inline iterator& operator++() {
            if (singleton_itr == owner->singleton_map.end()) {
                if ((++vector_itr) < (int) owner->mask_vec.size()) {
                    first = owner->mask_vec[vector_itr].mac;
                    second = &(owner->mask_vec[vector_itr].value);
                }
            } else if (++singleton_itr == owner->singleton_map.end()) {
                if ((++vector_itr) < (int) owner->mask_vec.size()) {
                    first = owner->mask_vec[vector_itr].mac;
                    second = &(owner->mask_vec[vector_itr].value);
                }
            } else {
                first = singleton_itr->first;
                second = &(singleton_itr->second);
            }

            return *this;
        }

        // Postfix
        inline iterator operator++(int) {
            iterator tmp = *this;
            ++*this;
            return tmp;
        }

        // equal
        inline bool operator==(const iterator& op) {
            return (singleton_itr == op.singleton_itr) && 
				(vector_itr == op.vector_itr);
        }

        // not
        inline bool operator!=(const iterator& op) {
            return (singleton_itr != op.singleton_itr) || 
				(vector_itr != op.vector_itr);
        }

        // pointer fake
        inline iterator *operator->() {
            return this;
        }

        mac_addr first;
        T *second;

    protected:
        inline void assign(typename map<mac_addr, T>::iterator in_itr) {
            singleton_itr = in_itr;
            vector_itr = -1;

            if (in_itr != owner->singleton_map.end()) {
                first = singleton_itr->first;
                second = &(singleton_itr->second);
            }
        }

        inline void assign(int in_itr) {
            singleton_itr = owner->singleton_map.end();
            vector_itr = in_itr;

            if (in_itr < (int) owner->mask_vec.size()) {
                first = owner->mask_vec[vector_itr].mac;
                second = &(owner->mask_vec[vector_itr].value);
            }
        }

        typename map<mac_addr, T>::iterator singleton_itr;
        int vector_itr;
        macmap<T> *owner;
    };

    friend class macmap<T>::iterator;

    inline iterator begin() {
        iterator ret(this);

        return ret;
    }

    inline iterator end() {
        iterator ret(this);
        ret.singleton_itr = singleton_map.end();
        ret.vector_itr = mask_vec.size();

        return ret;
    }

    // The caller will rebuild the index before using us...
    inline void fast_insert(mac_addr in_mac, T in_data) {
        // Single macs go into the singleton map
        if (in_mac.longmask == (uint64_t) -1) {
            singleton_map[in_mac] = in_data;
            return;
        }

        // Put them into the vector
        mask_vec_content content;
        content.mac = in_mac;
        content.value = in_data;
        mask_vec.push_back(content);
    }
    
    // This is a very expensive insert but it builds a system that allows
    // for fast searching, which is where we REALLY need the speed.
    inline void insert(mac_addr in_mac, T in_data) {
        // Single macs go into the singleton map
        if (in_mac.longmask == (uint64_t) -1) {
            singleton_map[in_mac] = in_data;
            return;
        }

        // Put them into the vector
        mask_vec_content content;
        content.mac = in_mac;
        content.value = in_data;
        mask_vec.push_back(content);

        reindex();
    }

    // Do a relatively fast find...
    inline iterator find(mac_addr in_mac) {
        iterator ret(this);

        if (in_mac.longmask == (uint64_t) -1) {
            // Look in the singleton map... This is very fast.
            typename map<mac_addr, T>::iterator sitr = singleton_map.find(in_mac);
            if (sitr != singleton_map.end()) {
                ret.assign(sitr);
                return ret;
            }
        }

        if (vec_offset_map.find(in_mac) != vec_offset_map.end()) {
            // We matched a large key in the vector map.  The vector is sorted
            // in decreasing granularity, so the first one we match we can count
            // as good and get out of here
            mask_vec_offsets oft = vec_offset_map[in_mac];
            for (unsigned int x = oft.last; x >= oft.first; x--) {
                if (in_mac <= mask_vec[x].mac) {
                    ret.assign(x);
                    return ret;
                }
            }
        }

        return end();
    }

    inline void erase(mac_addr in_mac) {
        iterator itr = find(in_mac);

        if (itr == end())
            return;

        if (itr.singleton_itr != singleton_map.end()) {
            singleton_map.erase(itr.singleton_itr);
            reindex();
            return;
        }

        if (itr.vector_itr >= 0 && itr.vector_itr < (int) mask_vec.size()) {
            mask_vec.erase(mask_vec.begin() + itr.vector_itr);
            reindex();
            return;
        }

    }

    inline T& operator[](mac_addr& index) {
        iterator foo = find(index);

        // This isn't very clean but its better than heap corruption 
        // and other horrible stuff
        if (foo == end()) {
            fprintf(stderr, "Something tried to use macmap[] to reference an "
                    "element that doesn't exist.  Fix me.\n");
            exit(1);
        }

        return *(foo->second);
    }

    int size() {
        return singleton_map.size() + mask_vec.size();
    }

	inline void clear(void) {
		vec_offset_map.clear();
		singleton_map.clear();
		mask_vec.clear();
	}

    inline void reindex(void) {
        // Order it
        if (mask_vec.size() == 0)
            return;

        stable_sort(mask_vec.begin(), mask_vec.end(), SortMaskVec());

        // Clear our old map of content
        vec_offset_map.clear();

        // Split it into offset groups
        mask_vec_offsets ofst;
        ofst.last = mask_vec.size() - 1;
        ofst.first = mask_vec.size() - 1;
        mac_addr owner = mask_vec[ofst.last].mac;
        for (unsigned int x = 0; x < mask_vec.size(); x++) {
            // Masked compare... is it still a subset of us?
            if (owner != mask_vec[x].mac) {
                vec_offset_map[owner] = ofst;
                ofst.first = x;
                ofst.last = x;
                owner = mask_vec[x].mac;
            } else {
                ofst.last = x;
            }
        }
        // Clean up the last stuff
        vec_offset_map[owner] = ofst;
        vec_offset_map[owner] = ofst;
    }

protected:
    map<mac_addr, T> singleton_map;
    vector<mask_vec_content> mask_vec;
    map<mac_addr, mask_vec_offsets> vec_offset_map;
};


#endif

