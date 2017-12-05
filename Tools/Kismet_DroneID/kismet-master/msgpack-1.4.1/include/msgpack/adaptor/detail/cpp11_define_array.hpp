//
// MessagePack for C++ static resolution routine
//
// Copyright (C) 2008-2013 FURUHASHI Sadayuki and KONDO Takatoshi
//
//    Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE_1_0.txt or copy at
//    http://www.boost.org/LICENSE_1_0.txt)
//
#ifndef MSGPACK_CPP11_DEFINE_ARRAY_HPP
#define MSGPACK_CPP11_DEFINE_ARRAY_HPP

#include "msgpack/versioning.hpp"
#include "msgpack/adaptor/adaptor_base.hpp"

// for MSGPACK_ADD_ENUM
#include "msgpack/adaptor/int.hpp"

#include <type_traits>
#include <tuple>

#define MSGPACK_DEFINE_ARRAY(...) \
    template <typename Packer> \
    void msgpack_pack(Packer& pk) const \
    { \
        msgpack::type::make_define_array(__VA_ARGS__).msgpack_pack(pk); \
    } \
    void msgpack_unpack(msgpack::object const& o) \
    { \
        msgpack::type::make_define_array(__VA_ARGS__).msgpack_unpack(o); \
    }\
    template <typename MSGPACK_OBJECT> \
    void msgpack_object(MSGPACK_OBJECT* o, msgpack::zone& z) const \
    { \
        msgpack::type::make_define_array(__VA_ARGS__).msgpack_object(o, z); \
    }

#define MSGPACK_BASE_ARRAY(base) (*const_cast<base *>(static_cast<base const*>(this)))

// MSGPACK_ADD_ENUM must be used in the global namespace.
#define MSGPACK_ADD_ENUM(enum_name) \
  namespace msgpack { \
  /** @cond */ \
  MSGPACK_API_VERSION_NAMESPACE(v1) { \
  /** @endcond */ \
  namespace adaptor { \
    template<> \
    struct convert<enum_name> { \
      msgpack::object const& operator()(msgpack::object const& o, enum_name& v) const { \
        std::underlying_type<enum_name>::type tmp; \
        o >> tmp; \
        v = static_cast<enum_name>(tmp);   \
        return o; \
      } \
    }; \
    template<> \
    struct object<enum_name> { \
      void operator()(msgpack::object& o, const enum_name& v) const { \
        auto tmp = static_cast<std::underlying_type<enum_name>::type>(v); \
        o << tmp; \
      } \
    }; \
    template<> \
    struct object_with_zone<enum_name> { \
      void operator()(msgpack::object::with_zone& o, const enum_name& v) const {  \
        auto tmp = static_cast<std::underlying_type<enum_name>::type>(v); \
        o << tmp; \
      } \
    }; \
    template <> \
    struct pack<enum_name> { \
      template <typename Stream> \
      msgpack::packer<Stream>& operator()(msgpack::packer<Stream>& o, const enum_name& v) const { \
        return o << static_cast<std::underlying_type<enum_name>::type>(v); \
      } \
    }; \
  } \
  /** @cond */ \
  } \
  /** @endcond */ \
  }

namespace msgpack {
/// @cond
MSGPACK_API_VERSION_NAMESPACE(v1) {
/// @endcond
namespace type {

template <typename Tuple, std::size_t N>
struct define_array_imp {
    template <typename Packer>
    static void pack(Packer& pk, Tuple const& t) {
        define_array_imp<Tuple, N-1>::pack(pk, t);
        pk.pack(std::get<N-1>(t));
    }
    static void unpack(msgpack::object const& o, Tuple& t) {
        define_array_imp<Tuple, N-1>::unpack(o, t);
        const size_t size = o.via.array.size;
        if(size <= N-1) { return; }
        o.via.array.ptr[N-1].convert(std::get<N-1>(t));
    }
    static void object(msgpack::object* o, msgpack::zone& z, Tuple const& t) {
        define_array_imp<Tuple, N-1>::object(o, z, t);
        o->via.array.ptr[N-1] = msgpack::object(std::get<N-1>(t), z);
    }
};

template <typename Tuple>
struct define_array_imp<Tuple, 1> {
    template <typename Packer>
    static void pack(Packer& pk, Tuple const& t) {
        pk.pack(std::get<0>(t));
    }
    static void unpack(msgpack::object const& o, Tuple& t) {
        const size_t size = o.via.array.size;
        if(size <= 0) { return; }
        o.via.array.ptr[0].convert(std::get<0>(t));
    }
    static void object(msgpack::object* o, msgpack::zone& z, Tuple const& t) {
        o->via.array.ptr[0] = msgpack::object(std::get<0>(t), z);
    }
};

template <typename... Args>
struct define_array {
    typedef define_array<Args...> value_type;
    typedef std::tuple<Args...> tuple_type;
    define_array(Args&... args) :
        a(args...) {}
    template <typename Packer>
    void msgpack_pack(Packer& pk) const
    {
        pk.pack_array(sizeof...(Args));

        define_array_imp<std::tuple<Args&...>, sizeof...(Args)>::pack(pk, a);
    }
    void msgpack_unpack(msgpack::object const& o)
    {
        if(o.type != msgpack::type::ARRAY) { throw msgpack::type_error(); }

        define_array_imp<std::tuple<Args&...>, sizeof...(Args)>::unpack(o, a);
    }
    void msgpack_object(msgpack::object* o, msgpack::zone& z) const
    {
        o->type = msgpack::type::ARRAY;
        o->via.array.ptr = static_cast<msgpack::object*>(z.allocate_align(sizeof(msgpack::object)*sizeof...(Args)));
        o->via.array.size = sizeof...(Args);

        define_array_imp<std::tuple<Args&...>, sizeof...(Args)>::object(o, z, a);
    }

    std::tuple<Args&...> a;
};

template <>
struct define_array<> {
    typedef define_array<> value_type;
    typedef std::tuple<> tuple_type;
    template <typename Packer>
    void msgpack_pack(Packer& pk) const
    {
        pk.pack_array(0);
    }
    void msgpack_unpack(msgpack::object const& o)
    {
        if(o.type != msgpack::type::ARRAY) { throw msgpack::type_error(); }
    }
    void msgpack_object(msgpack::object* o, msgpack::zone&) const
    {
        o->type = msgpack::type::ARRAY;
        o->via.array.ptr = NULL;
        o->via.array.size = 0;
    }
};

inline define_array<> make_define_array()
{
    return define_array<>();
}

template <typename... Args>
inline define_array<Args...> make_define_array(Args&... args)
{
    return define_array<Args...>(args...);
}

}  // namespace type
/// @cond
}  // MSGPACK_API_VERSION_NAMESPACE(v1)
/// @endcond
}  // namespace msgpack

#endif // MSGPACK_CPP11_DEFINE_ARRAY_HPP
