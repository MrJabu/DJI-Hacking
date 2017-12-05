require 'rubygems'
require 'json'

# There are *hidden* / unused feeds. ExternalBeta is currently NOT used. Alpha was recently *lit up*
# $ curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/channels/
# {"count":4,"next":null,"previous":null,"results":[{"name":"Production","id":1},{"name":"Beta","id":2},{"name":"ExternalBeta","id":3},{"name":"Alpha","id":5}]}
# {"name":"SitescanAlpha","id":6},{"name":"SiteScanExternalBeta","id":7},{"name":"SiteScanProduction","id":8},{"name":"-","id":10}]}


beta = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=Beta 2>/dev/null]
prod = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=Production 2>/dev/null]
alpha = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=Alpha 2>/dev/null]
externalbeta = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=ExternalBeta 2>/dev/null]
sitescanalpha = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=SitescanAlpha 2>/dev/null]
sitescanexternalbeta = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=SiteScanExternalBeta 2>/dev/null]
sitescanproduction = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=SiteScanProduction 2>/dev/null]
minus = %x[curl -H "Authorization: Token bd02efe84c11b41d11c4644e9b04ad4673deb6df" https://firmwarehouse.3dr.com/products/?channel=- 2>/dev/null]


def print_relnotes(info)
	parsed = JSON.parse(info)

	parsed["results"].each do |result|
	  releases = result["releases"].each do |release|
		  unless release.nil?
		    puts release["release_notes"]
		    puts release["file"]
		  else
		#    puts "There is no release!"
		  end
	  end
	end
end

if ARGV.length < 1
 puts "ruby #{ARGV[0]} [prod|beta|alpha|externalbeta|sitescanalpha|sitescanexternalbeta|sitescanproduction|minus|all]"
end 

if ARGV[0] == "prod"
  print_relnotes(prod)
elsif ARGV[0] == "beta"
  print_relnotes(beta)
elsif ARGV[0] == "alpha"
  print_relnotes(alpha)
elsif ARGV[0] == "externalbeta"
  print_relnotes(externalbeta)
elsif ARGV[0] == "sitescanalpha"
  print_relnotes(sitescanalpha)
elsif ARGV[0] == "sitescanexternalbeta"
  print_relnotes(sitescanexternalbeta)
elsif ARGV[0] == "sitescanproduction"
  print_relnotes(sitescanproduction)
elsif ARGV[0] == "minus"
  print_relnotes(minus)
elsif ARGV[0] == "all"
  print_relnotes(beta)
  print_relnotes(prod)
  print_relnotes(alpha)
  print_relnotes(externalbeta)
  print_relnotes(sitescanalpha)
  print_relnotes(sitescanexternalbeta)
  print_relnotes(sitescanproduction)
  print_relnotes(minus)

end

