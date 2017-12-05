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

#include "config.h"

#include "util.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <fcntl.h>
#include <stdarg.h>
#include <math.h>
#include <string.h>

#ifdef HAVE_LIBUTIL_H
# include <libutil.h>
#endif /* HAVE_LIBUTIL_H */

#if PF_ARGV_TYPE == PF_ARGV_PSTAT
#error "pstat?"
#endif

#if PF_ARGV_TYPE == PF_ARGV_PSTAT
# ifdef HAVE_SYS_PSTAT_H
#  include <sys/pstat.h>
# else
#  undef PF_ARGV_TYPE
#  define PF_ARGV_TYPE PF_ARGV_WRITEABLE
# endif /* HAVE_SYS_PSTAT_H */
#endif /* PF_ARGV_PSTAT */

#if PF_ARGV_TYPE == PF_ARGV_PSSTRINGS
# ifndef HAVE_SYS_EXEC_H
#  undef PF_ARGV_TYPE
#  define PF_ARGV_TYPE PF_ARGV_WRITEABLE
# else
#  include <machine/vmparam.h>
#  include <sys/exec.h>
# endif /* HAVE_SYS_EXEC_H */
#endif /* PF_ARGV_PSSTRINGS */

// We need this to make uclibc happy since they don't even have rintf...
#ifndef rintf
#define rintf(x) (float) rint((double) (x))
#endif

#include <sstream>
#include <iomanip>
#include <stdexcept>

#include "packet.h"

// Munge text down to printable characters only.  Simpler, cleaner munger than
// before (and more blatant when munging)
string MungeToPrintable(const char *in_data, unsigned int max, int nullterm) {
	string ret;
	unsigned int i;

	for (i = 0; i < max; i++) {
		if ((unsigned char) in_data[i] == 0 && nullterm == 1)
			return ret;

		if ((unsigned char) in_data[i] >= 32 && (unsigned char) in_data[i] <= 126) {
			ret += in_data[i];
		} else {
			ret += '\\';
			ret += ((in_data[i] >> 6) & 0x03) + '0';
			ret += ((in_data[i] >> 3) & 0x07) + '0';
			ret += ((in_data[i] >> 0) & 0x07) + '0';
		}
	}

	return ret;
}

string MungeToPrintable(string in_str) {
	return MungeToPrintable(in_str.c_str(), in_str.length(), 1);
}

string StrLower(string in_str) {
    string thestr = in_str;
    for (unsigned int i = 0; i < thestr.length(); i++)
        thestr[i] = tolower(thestr[i]);

    return thestr;
}

string StrUpper(string in_str) {
    string thestr = in_str;

    for (unsigned int i = 0; i < thestr.length(); i++)
        thestr[i] = toupper(thestr[i]);

    return thestr;
}

string StrStrip(string in_str) {
    string temp;
    unsigned int start, end;

    start = 0;
    end = in_str.length();

    if (in_str[0] == '\n')
        return "";

    for (unsigned int x = 0; x < in_str.length(); x++) {
        if (in_str[x] != ' ' && in_str[x] != '\t') {
            start = x;
            break;
        }
    }
    for (unsigned int x = in_str.length(); x > 1; ) {
		x--;
        if (in_str[x] != ' ' && in_str[x] != '\t' && in_str[x] != '\n') {
            end = x;
            break;
        }
    }

    return in_str.substr(start, end-start+1);
}

string StrPrintable(string in_str) {
    string thestr;

    for (unsigned int i = 0; i < in_str.length(); i++) {
		if (isprint(in_str[i])) {
			thestr += in_str[i];
		}
	}

    return thestr;
}

int IsBlank(const char *s) {
    int len, i;

    if (NULL == s) { 
		return 1; 
	}

    if (0 == (len = strlen(s))) { 
		return 1; 
	}

    for (i = 0; i < len; ++i) {
        if (' ' != s[i]) { 
			return 0; 
		}
    }

    return 1;
}

string AlignString(string in_txt, char in_spacer, int in_align, int in_width) {
	if (in_align == 1) {
		// Center -- half, text, chop to fit
		int sp = (in_width / 2) - (in_txt.length() / 2);
		string ts = "";

		if (sp > 0) {
			ts = string(sp, in_spacer);
		}

		ts += in_txt.substr(0, in_width);

		return ts;
	} else if (in_align == 2) {
		// Right -- width - len, chop to fit
		int sp = (in_width - in_txt.length());
		string ts = "";

		if (sp > 0) {
			ts = string(sp, in_spacer);
		}

		ts += in_txt.substr(0, in_width);

		return ts;
	}

	// Left align -- make sure it's not too long
	return in_txt.substr(0, in_width);
}

int HexStrToUint8(string in_str, uint8_t *in_buf, int in_buflen) {
	int decode_pos = 0;
	int str_pos = 0;

	while ((unsigned int) str_pos < in_str.length() && decode_pos < in_buflen) {
		short int tmp;

		if (in_str[str_pos] == ' ') {
			str_pos++;
			continue;
		}

		if (sscanf(in_str.substr(str_pos, 2).c_str(), "%2hx", &tmp) != 1) {
			return -1;
		}

		in_buf[decode_pos++] = tmp;
		str_pos += 2;
	}

	return decode_pos;
}

int XtoI(char x) {
    if (isxdigit(x)) {
        if (x <= '9')
            return x - '0';
        return toupper(x) - 'A' + 10;
    }

    return -1;
}

int Hex2UChar(unsigned char *in_hex, unsigned char *in_chr) {
    memset(in_chr, 0, sizeof(unsigned char) * WEPKEY_MAX);
    int chrpos = 0;

    for (unsigned int strpos = 0; strpos < WEPKEYSTR_MAX && chrpos < WEPKEY_MAX; strpos++) {
        if (in_hex[strpos] == 0)
            break;

        if (in_hex[strpos] == ':')
            strpos++;

        // Assume we're going to eat the pair here
        if (isxdigit(in_hex[strpos])) {
            if (strpos > (WEPKEYSTR_MAX - 2))
                return 0;

            int d1, d2;
            if ((d1 = XtoI(in_hex[strpos++])) == -1)
                return 0;
            if ((d2 = XtoI(in_hex[strpos])) == -1)
                return 0;

            in_chr[chrpos++] = (d1 * 16) + d2;
        }

    }

    return(chrpos);
}

// Complex string tokenizer which understands nested delimiters, such as 
// "foo","bar","baz,foo",something
// and network protocols like
// foo bar \001baz foo\001
vector<smart_word_token> BaseStrTokenize(string in_str, 
										 string in_split, string in_quote) {
	size_t begin = 0;
	size_t end = 0;
	vector<smart_word_token> ret;
    smart_word_token stok;
	int special = 0;
	string val;
	
	if (in_str.length() == 0)
		return ret;

	for (unsigned int x = 0; x < in_str.length(); x++) {
		if (in_str.find(in_quote, x) == x) {
			if (special == 0) {
				// reset beginning on string if we're in a special block
				begin = x;
				special = 1;
			} else {
				special = 0;
			}

			continue;
		}

		if (special == 0 && in_str.find(in_split, x) == x) {
			stok.begin = begin;
			stok.end = end;
			stok.word = val;

			ret.push_back(stok);

			val = "";
			x += in_split.length() - 1;

			begin = x;

			continue;
		}

		val += in_str[x];
		end = x;
	}

	stok.begin = begin;
	stok.end = end;
	stok.word = val;
	ret.push_back(stok);

	return ret;
}

// No-frills tokenize with no intelligence about nested delimiters
vector<string> StrTokenize(string in_str, string in_split, int return_partial) {
    size_t begin = 0;
    size_t end = in_str.find(in_split);
    vector<string> ret;

    if (in_str.length() == 0)
        return ret;
    
    while (end != string::npos) {
        string sub = in_str.substr(begin, end-begin);
        begin = end+1;
        end = in_str.find(in_split, begin);
        ret.push_back(sub);
    }

    if (return_partial && begin != in_str.size())
        ret.push_back(in_str.substr(begin, in_str.size() - begin));

    return ret;
}

string StrJoin(vector<string> in_content, string in_delim, bool in_first) {
    ostringstream ostr;

    bool d = false;

    for (auto x = in_content.begin(); x != in_content.end(); ++x) {
        if (d || in_first)
            ostr << in_delim;

        if (!d)
            d = true;

        ostr << (*x);
    }

    return ostr.str();
}

// Collapse into basic tokenizer
vector<smart_word_token> NetStrTokenize(string in_str, string in_split, 
										int return_partial) {
	return BaseStrTokenize(in_str, in_split, "\001");
}

// Collapse into basic tokenizer rewrite
vector<string> QuoteStrTokenize(string in_str, string in_split) {
	vector<string> ret;
	vector<smart_word_token> bret;

	bret = BaseStrTokenize(in_str, in_split, "\"");

	for (unsigned int b = 0; b < bret.size(); b++) {
		ret.push_back(bret[b].word);
	}

	return ret;
}

int TokenNullJoin(string *ret_str, const char **in_list) {
	int ret = 0;

	while (in_list[ret] != NULL) {
		(*ret_str) += in_list[ret];

		if (in_list[ret + 1] != NULL)
			(*ret_str) += ",";

		ret++;
	}

	return ret;
}

// Find an option - just like config files
string FetchOpt(string in_key, vector<opt_pair> *in_vec) {
	string lkey = StrLower(in_key);

	if (in_vec == NULL)
		return "";

	for (unsigned int x = 0; x < in_vec->size(); x++) {
		if ((*in_vec)[x].opt == lkey)
			return (*in_vec)[x].val;
	}

	return "";
}

int FetchOptBoolean(string in_key, vector<opt_pair> *in_vec, int dvalue) {
	string s = FetchOpt(in_key, in_vec);

	return StringToBool(s, dvalue);
}

// Quick fetch of strings from a map of options
string FetchOpt(string in_key, map<string, string> in_map, string dvalue) {
    auto i = in_map.find(in_key);

    if (i == in_map.end())
        return dvalue;

    return i->second;
}

int FetchOptBoolean(string in_key, map<string, string> in_map, int dvalue) {
    auto i = in_map.find(in_key);

    if (i == in_map.end())
        return dvalue;

    return StringToBool(i->second, dvalue);
}

vector<string> FetchOptVec(string in_key, vector<opt_pair> *in_vec) {
	string lkey = StrLower(in_key);
	vector<string> ret;

	if (in_vec == NULL)
		return ret;

	for (unsigned int x = 0; x < in_vec->size(); x++) {
		if ((*in_vec)[x].opt == lkey)
			ret.push_back((*in_vec)[x].val);
	}

	return ret;
}

int StringToOpts(string in_line, string in_sep, vector<opt_pair> *in_vec) {
	vector<string> optv;
	opt_pair optp;

	int in_tag = 1, in_quote = 0;
	
	optp.quoted = 0;

	for (unsigned int x = 0; x < in_line.length(); x++) {
		if (in_tag && in_line[x] != '=') {
			optp.opt += in_line[x];
			continue;
		}

		if (in_tag && in_line[x] == '=') {
			in_tag = 0;
			continue;
		}

		if (in_line[x] == '"') {
			if (in_quote == 0) {
				in_quote = 1;
				optp.quoted = 1;
			} else {
				in_quote = 0;
			}

			continue;
		}

		if (in_quote == 0 && in_line[x] == in_sep[0]) {
			in_vec->push_back(optp);
			optp.quoted = 0;
			optp.opt = "";
			optp.val = "";
			in_tag = 1;
			continue;
		}

		optp.val += in_line[x];
	}

	in_vec->push_back(optp);

	return 1;
}

void AddOptToOpts(string opt, string val, vector<opt_pair> *in_vec) {
	opt_pair optp;

	optp.opt = StrLower(opt);
	optp.val = val;

	in_vec->push_back(optp);
}

void ReplaceAllOpts(string opt, string val, vector<opt_pair> *in_vec) {
	opt_pair optp;

	optp.opt = StrLower(opt);
	optp.val = val;

	for (unsigned int x = 0; x < in_vec->size(); x++) {
		if ((*in_vec)[x].val == optp.val) {
			in_vec->erase(in_vec->begin() + x);
			x--;
			continue;
		}
	}

	in_vec->push_back(optp);
}

vector<string> LineWrap(string in_txt, unsigned int in_hdr_len, 
						unsigned int in_maxlen) {
	vector<string> ret;

	size_t pos, prev_pos, start, hdroffset;
	start = hdroffset = 0;

	for (pos = prev_pos = in_txt.find(' ', in_hdr_len); pos != string::npos; 
		 pos = in_txt.find(' ', pos + 1)) {
		if ((hdroffset + pos) - start >= in_maxlen) {
			if (pos - prev_pos > (in_maxlen / 4)) {
				pos = prev_pos = start + (in_maxlen - hdroffset);
			}

			string str(hdroffset, ' ');
			hdroffset = in_hdr_len;
			str += in_txt.substr(start, prev_pos - start);
			ret.push_back(str);
			
			start = prev_pos;
		}

		prev_pos = pos + 1;
	}

	while (in_txt.length() - start > (in_maxlen - hdroffset)) {
		string str(hdroffset, ' ');
		hdroffset = in_hdr_len;

		str += in_txt.substr(start, (prev_pos - start));
		ret.push_back(str);

		start = prev_pos;

		prev_pos+= (in_maxlen - hdroffset);
	}

	string str(hdroffset, ' ');
	str += in_txt.substr(start, in_txt.length() - start);
	ret.push_back(str);

	return ret;
}

string InLineWrap(string in_txt, unsigned int in_hdr_len, 
				  unsigned int in_maxlen) {
	vector<string> raw = LineWrap(in_txt, in_hdr_len, in_maxlen);
	string ret;

	for (unsigned int x = 0; x < raw.size(); x++) {
		ret += raw[x] + "\n";
	}

	return ret;
}

string SanitizeXML(string in_str) {
	// Ghetto-fied XML sanitizer.  Add more stuff later if we need to.
	string ret;
	for (unsigned int x = 0; x < in_str.length(); x++) {
		if (in_str[x] == '&')
			ret += "&amp;";
		else if (in_str[x] == '<')
			ret += "&lt;";
		else if (in_str[x] == '>')
			ret += "&gt;";
		else
			ret += in_str[x];
	}

	return ret;
}

string SanitizeCSV(string in_str) {
	string ret;
	for (unsigned int x = 0; x < in_str.length(); x++) {
		if (in_str[x] == ';')
			ret += " ";
		else
			ret += in_str[x];
	}

	return ret;
}

void Float2Pair(float in_float, int16_t *primary, int64_t *mantissa) {
    *primary = (int) in_float;
    *mantissa = (long) (1000000 * ((in_float) - *primary));
}

float Pair2Float(int16_t primary, int64_t mantissa) {
    return (double) primary + ((double) mantissa / 1000000);
}

vector<int> Str2IntVec(string in_text) {
    vector<string> optlist = StrTokenize(in_text, ",");
    vector<int> ret;
    int ch;

    for (unsigned int x = 0; x < optlist.size(); x++) {
        if (sscanf(optlist[x].c_str(), "%d", &ch) != 1) {
            ret.clear();
            break;
        }

        ret.push_back(ch);
    }

    return ret;
}

#ifdef SYS_LINUX
int FetchSysLoadAvg(uint8_t *in_avgmaj, uint8_t *in_avgmin) {
    FILE *lf;
    short unsigned int tmaj, tmin;

    if ((lf = fopen("/proc/loadavg", "r")) == NULL) {
        fclose(lf);
        return -1;
    }

    if (fscanf(lf, "%hu.%hu", &tmaj, &tmin) != 2) {
        fclose(lf);
        return -1;
    }

    (*in_avgmaj) = tmaj;
    (*in_avgmin) = tmin;

    fclose(lf);

    return 1;
}
#endif

uint32_t Adler32IncrementalChecksum(const char *in_buf, size_t in_len,
        uint32_t *s1, uint32_t *s2) {
    size_t i;
    const uint8_t *buf = (const uint8_t *) in_buf;
    int CHAR_OFFSET = 0;

    if (in_len < 4)
        return 0;

    for (i = 0; i < (in_len - 4); i += 4) {
        *s2 += 4 * (*s1 + buf[i]) + 3 * buf[i + 1] + 
            2 * buf[i+2] + buf[i + 3] + 
            10 * CHAR_OFFSET;
        *s1 += (buf[i + 0] + buf[i + 1] + buf[i + 2] + 
                buf[i + 3] + 4 * CHAR_OFFSET); 
    }

    for (; i < in_len; i++) {
        *s1 += (buf[i] + CHAR_OFFSET); 
        *s2 += *s1;
    }

    return (*s1 & 0xffff) + (*s2 << 16);
}

uint32_t Adler32Checksum(std::string in_buf) {
    return Adler32Checksum(in_buf.data(), in_buf.length());
}

uint32_t Adler32Checksum(const char *in_buf, size_t in_len) {
    uint32_t s1, s2;

    s1 = 0;
    s2 = 0;

    return Adler32IncrementalChecksum(in_buf, in_len, &s1, &s2);
}

list<_kis_lex_rec> LexString(string in_line, string& errstr) {
	list<_kis_lex_rec> ret;
	int curstate = _kis_lex_none;
	_kis_lex_rec cpr;
	string tempstr;
	char lastc = 0;
	char c = 0;

	cpr.type = _kis_lex_none;
	cpr.data = "";
	ret.push_back(cpr);

	for (size_t pos = 0; pos < in_line.length(); pos++) {
		lastc = c;
		c = in_line[pos];

		cpr.data = "";

		if (curstate == _kis_lex_none) {
			// Open paren
			if (c == '(') {
				cpr.type = _kis_lex_popen;
				ret.push_back(cpr);
				continue;
			}

			// Close paren
			if (c == ')') {
				cpr.type = _kis_lex_pclose;
				ret.push_back(cpr);
				continue;
			}

			// Negation
			if (c == '!') {
				cpr.type = _kis_lex_negate;
				ret.push_back(cpr);
				continue;
			}

			// delimiter
			if (c == ',') {
				cpr.type = _kis_lex_delim;
				ret.push_back(cpr);
				continue;
			}

			// start a quoted string
			if (c == '"') {
				curstate = _kis_lex_quotestring;
				tempstr = "";
				continue;
			}
		
			curstate = _kis_lex_string;
			tempstr = c;
			continue;
		}

		if (curstate == _kis_lex_quotestring) {
			// We don't close on an escaped \"
			if (c == '"' && lastc != '\\') {
				// Drop out of the string and make the lex stack element
				curstate = _kis_lex_none;
				cpr.type = _kis_lex_quotestring;
				cpr.data = tempstr;
				ret.push_back(cpr);

				tempstr = "";

				continue;
			}

			// Add it to the quoted temp strnig
			tempstr += c;
		}

		if (curstate == _kis_lex_string) {
			// If we're a special character break out and add the lex stack element
			// otherwise increase our unquoted string
			if (c == '(' || c == ')' || c == '!' || c == '"' || c == ',') {
				cpr.type = _kis_lex_string;
				cpr.data = tempstr;
				ret.push_back(cpr);
				tempstr = "";
				curstate = _kis_lex_none;
				pos--;
				continue;
			}

			tempstr += c;
			continue;
		}
	}

	if (curstate == _kis_lex_quotestring) {
		errstr = "Unfinished quoted string in line '" + in_line + "'";
		ret.clear();
	}

	return ret;
}

// Taken from the BBN USRP 802.11 encoding code
unsigned int update_crc32_80211(unsigned int crc, const unsigned char *data,
								int len, unsigned int poly) {
	int i, j;
	unsigned short ch;

	for ( i = 0; i < len; ++i) {
		ch = data[i];
		for (j = 0; j < 8; ++j) {
			if ((crc ^ ch) & 0x0001) {
				crc = (crc >> 1) ^ poly;
			} else {
				crc = (crc >> 1);
			}
			ch >>= 1;
		}
	}
	return crc;
}

void crc32_init_table_80211(unsigned int *crc32_table) {
	int i;
	unsigned char c;

	for (i = 0; i < 256; ++i) {
		c = (unsigned char) i;
		crc32_table[i] = update_crc32_80211(0, &c, 1, IEEE_802_3_CRC32_POLY);
	}
}

unsigned int crc32_le_80211(unsigned int *crc32_table, const unsigned char *buf, 
							int len) {
	int i;
	unsigned int crc = 0xFFFFFFFF;

	for (i = 0; i < len; ++i) {
		crc = (crc >> 8) ^ crc32_table[(crc ^ buf[i]) & 0xFF];
	}

	crc ^= 0xFFFFFFFF;

	return crc;
}

void SubtractTimeval(struct timeval *in_tv1, struct timeval *in_tv2,
					 struct timeval *out_tv) {
	if (in_tv1->tv_sec < in_tv2->tv_sec ||
		(in_tv1->tv_sec == in_tv2->tv_sec && in_tv1->tv_usec < in_tv2->tv_usec) ||
		in_tv1->tv_sec == 0 || in_tv2->tv_sec == 0) {
		out_tv->tv_sec = 0;
		out_tv->tv_usec = 0;
		return;
	}

	if (in_tv2->tv_usec > in_tv1->tv_usec) {
		out_tv->tv_usec = 1000000 + in_tv1->tv_usec - in_tv2->tv_usec;
		out_tv->tv_sec = in_tv1->tv_sec - in_tv2->tv_sec - 1;
	} else {
		out_tv->tv_usec = in_tv1->tv_usec - in_tv2->tv_usec;
		out_tv->tv_sec = in_tv1->tv_sec - in_tv2->tv_sec;
	}
}

/* Airware PPI gps conversion code from Johnny Csh */

/*
 * input: a unsigned 32-bit (native endian) value between 0 and 3600000000 (inclusive)
 * output: a signed floating point value betwen -180.0000000 and + 180.0000000, inclusive)
 */
double fixed3_7_to_double(u_int32_t in) {
    int32_t remapped_in = in - (180 * 10000000);
    double ret = (double) ((double) remapped_in / 10000000);
    return ret;
}
/*
 * input: a native 32 bit unsigned value between 0 and 999999999
 * output: a positive floating point value between 000.0000000 and 999.9999999
 */
double fixed3_6_to_double(u_int32_t in) {
    double ret = (double) in  / 1000000.0;
    return ret;
}
/*
 * input: a native 32 bit unsigned value between 0 and 999.999999
 * output: a signed floating point value between -180000.0000 and +180000.0000
 */
double fixed6_4_to_double(u_int32_t in) {
    int32_t remapped_in = in - (180000 * 10000);
    double ret = (double) ((double) remapped_in / 10000);
    return ret;
}
/*
 * input: a native 32 bit nano-second counter
 * output: a signed floating point second counter
 */
double ns_to_double(u_int32_t in) {
    double ret;
    ret = (double) in / 1000000000;
    return ret;
}

/*
 * input: a signed floating point value betwen -180.0000000 and + 180.0000000, inclusive)
 * output: a unsigned 32-bit (native endian) value between 0 and 3600000000 (inclusive)
 */
u_int32_t double_to_fixed3_7(double in) 
{
    if (in < -180 || in >= 180) 
        return 0;
    //This may be positive or negative.
    int32_t scaled_in =  (int32_t) ((in) * (double) 10000000); 
    //If the input conditions are met, this will now always be positive.
    u_int32_t  ret = (u_int32_t) (scaled_in +  ((int32_t) 180 * 10000000)); 
    return ret;
}
/*
 * input: a signed floating point value betwen -180000.0000 and + 180000.0000, inclusive)
 * output: a unsigned 32-bit (native endian) value between 0 and 3600000000 (inclusive)
 */
u_int32_t double_to_fixed6_4(double in) 
{
    if (in < -180000.0001 || in >= 180000.0001) 
        return 0;
    //This may be positive or negative.
    int32_t scaled_in =  (int32_t) ((in) * (double) 10000); 
    //If the input conditions are met, this will now always be positive.
    u_int32_t  ret = (u_int32_t) (scaled_in +  ((int32_t) 180000 * 10000)); 
    return ret;
}
/*
 * input: a positive floating point value between 000.0000000 and 999.9999999
 * output: a native 32 bit unsigned value between 0 and 999999999
 */
u_int32_t double_to_fixed3_6(double in) {
    u_int32_t ret = (u_int32_t) (in  * (double) 1000000.0);
    return ret;
}

/*
 * input: a signed floating point second counter
 * output: a native 32 bit nano-second counter
 */
u_int32_t double_to_ns(double in) {
    u_int32_t ret;
    ret =  in * (double) 1000000000;
    return ret;
}

int StringToBool(string s, int dvalue) {
	string ls = StrLower(s);

	if (ls == "true" || ls == "t") {
		return 1;
	} else if (ls == "false" || ls == "f") {
		return 0;
	}

	return dvalue;
}

int StringToInt(string s) {
    int r;

    if (sscanf(s.c_str(), "%d", &r) != 1)
        throw(std::runtime_error("not an integer"));

    return r;
}

unsigned int StringToUInt(string s) {
    unsigned int r;

    if (sscanf(s.c_str(), "%u", &r) != 1)
        throw(std::runtime_error("not an unsigned integer"));

    return r;
}

string StringAppend(string s, string a, string d) {
	if (s.length() == 0)
		return a;

	if (s.length() > d.length() &&
		s.substr(s.length() - d.length(), d.length()) == d)
		return s + a;

	return s + d + a;
}

int GetLengthTagOffsets(unsigned int init_offset, 
						kis_datachunk *in_chunk,
						map<int, vector<int> > *tag_cache_map) {
    int cur_tag = 0;
    // Initial offset is 36, that's the first tag
    unsigned int cur_offset = (unsigned int) init_offset;
    uint8_t len;

    // Bail on invalid incoming offsets
    if (init_offset >= in_chunk->length) {
        return -1;
	}
    
    // If we haven't parsed the tags for this frame before, parse them all now.
    // Return an error code if one of them is malformed.
    if (tag_cache_map->size() == 0) {
        while (1) {
            // Are we over the packet length?
            if (cur_offset + 2 >= in_chunk->length) {
                break;
            }

            // Read the tag we're on and bail out if we're done
            cur_tag = (int) in_chunk->data[cur_offset];

            // Move ahead one byte and read the length.
            len = (in_chunk->data[cur_offset+1] & 0xFF);

            // If this is longer than we have...
            if ((cur_offset + len + 2) > in_chunk->length) {
                return -1;
            }

            // (*tag_cache_map)[cur_tag] = cur_offset + 1;
			
            (*tag_cache_map)[cur_tag].push_back(cur_offset + 1);

            // Jump the length+length byte, this should put us at the next tag
            // number.
            cur_offset += len+2;
        }
    }
    
    return 0;
}

std::string MultiReplaceAll(std::string in, std::string match, 
        std::string repl) {
    for (size_t pos = 0; (pos = in.find(match, pos)) != std::string::npos;
            pos += repl.length()) {
        in.replace(pos, match.length(), repl);
    }

    return in;
}

string kis_strerror_r(int errnum) {
    char *d_errstr = new char[1024];
    string rs;

    // How did glibc get this so amazingly wrong?

#if (_POSIX_C_SOURCE >= 200112L || _XOPEN_SOURCE >= 600) && ! _GNU_SOURCE
    int r;
    r = strerror_r(errnum, d_errstr, 1024);

    if (r == 0)
        rs = string(d_errstr);
    
    delete[] d_errstr;
    return rs;
#else
    if (strerror_r(errnum, d_errstr, 1024)) {
        rs = IntToString(errnum);
    } else {
        rs = string(d_errstr);
    }
    delete[] d_errstr;
    return rs;
#endif
}

double ts_to_double(struct timeval ts) {
    return (double) ts.tv_sec + (double) ((double) ts.tv_usec / (double) 1000000);
}

double ts_now_to_double() {
    struct timeval ts;
    gettimeofday(&ts, NULL);
    return (double) ts.tv_sec + (double) ((double) ts.tv_usec / (double) 1000000);
}

std::string hexstr_to_binstr(const char *hs) {
    size_t len = strlen(hs) / 2;
    size_t p = 0, sp = 0;
    char t;

    if (strlen(hs) % 2 == 1)
        len++;

    std::string r("\0", len);

    if (strlen(hs) % 2 == 1) {
        sscanf(&(hs[0]), "%1hhx", &t);
        r[0] = t;
        p = 1;
        sp = 1;
    }

    for (/* */; p < len && sp < strlen(hs); p++, sp += 2) {
        sscanf(&(hs[sp]), "%2hhx", &t);
        r[p] = t;
    }

    return r;
}
