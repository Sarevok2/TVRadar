package XMLTV::Supplement;

use strict;

BEGIN {
    use Exporter   ();
    our (@ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS);
    
    @ISA         = qw(Exporter);
    @EXPORT      = qw( );
    %EXPORT_TAGS = ( );     # eg: TAG => [ qw!name1 name2! ],
    @EXPORT_OK   = qw/GetSupplement SetSupplementRoot/;
}
our @EXPORT_OK;

use File::Slurp qw/read_file/;
use File::Spec;
use File::Path;
use LWP::UserAgent;
use HTTP::Status qw/RC_NOT_MODIFIED RC_OK/;
use XMLTV; # We only need VERSION...

=head1 NAME

XMLTV::Supplement

=head1 DESCRIPTION

Utility library that loads supplementary files for xmltv-grabbers and other
programs in the xmltv-distribution.

Supplementary files can be loaded either via http or from a local
file, depending on the configuration of the module. The default is to
load the files from http://supplement.xmltv.org. This can be
changed by setting the environment variable XMLTV_SUPPLEMENT to the
new root-directory or root-url for supplementary files.

=head1 EXPORTED FUNCTIONS

All these functions are exported on demand.

=over 4

=cut

sub d {
  print STDERR "XMLTV::Supplement: $_[0]\n" if $ENV{XMLTV_SUPPLEMENT_VERBOSE};
}

my $cachedir;

sub create_cachedir {
  my $base;
  if ($^O eq 'MSWin32') {
    require Win32;
    Win32->import( qw(CSIDL_LOCAL_APPDATA) ); 
    $base =  Win32::GetFolderPath( CSIDL_LOCAL_APPDATA() );
    $base =~ s/ /\ /g;
    if( not -d $base ) {
      $base =~ s/(.*?)\\Local Settings(.*)/$1$2/;
    } elsif( not -d $base ) {
      die "Unable to find suitable cache-directory: $base";
      exit 1;
    }
    $cachedir = File::Spec->catfile( $base, "xmltv", "supplement" );
  }
  else {
    $cachedir = File::Spec->catfile( $ENV{HOME}, ".xmltv", "supplement" );
  }

  d( "Using cachedir '$cachedir'" );
  create_dir( $cachedir );
}

sub create_dir {
  my( $dir ) = @_;

  eval { mkpath($dir) };
  if ($@) {
    print STDERR "Failed to create $dir: $@";
    exit 1;
  }
}

my $supplement_root;

sub set_supplement_root {
  if( defined( $ENV{XMLTV_SUPPLEMENT} ) ) {
    $supplement_root = $ENV{XMLTV_SUPPLEMENT};
  }
  else {
    $supplement_root = "http://supplement.xmltv.org/";
  }
}

my $ua;

sub init_ua {
  $ua = LWP::UserAgent->new( agent => "XMLTV::Supplement/" . $XMLTV::VERSION );
  $ua->env_proxy();
}

=item GetSupplement

Load a supplement file and return it as a string. Takes two parameters: 
directory and filename.

    my $content = GetSupplement( 'tv_grab_uk_rt', 'channel_ids' );

GetSupplement will always return a string with the content. If it fails
to get the content, it prints an error-message and aborts the program.

=cut

sub GetSupplement {
  my( $directory, $name ) = @_;

  set_supplement_root() if not defined $supplement_root;

  if( $supplement_root =~ m%^http(s){0,1}://% ) {
    return GetSupplementUrl( $directory, $name );
  }
  else {
    return GetSupplementFile( $directory, $name );
  }
}

sub GetSupplementFile {
  my( $directory, $name ) = @_;

  my $filename;

  if( defined( $directory ) ) {
    $filename = File::Spec->catfile( $supplement_root, $directory, $name );
  }
  else {
    $filename = File::Spec->catfile( $supplement_root, $name );
  }

  my $result;

  d( "Reading $filename" );
  eval { $result = read_file( $filename ) };

  if( not defined( $result ) ) {
    print STDERR "XMLTV::Supplement: Failed to read from $filename.\n";
    exit 1;
  }

  return $result;
}

sub GetSupplementUrl {
  my( $directory, $name ) = @_;
  
  create_cachedir() if not defined $cachedir;
  init_ua() if not defined $ua;

  my $dir;
  if( defined( $directory ) ) {
    $dir = File::Spec->catfile( $cachedir, $directory );
    create_dir( $dir );
  }
  else {
    $dir = $cachedir;
  }

  # Remove trailing slash
  $supplement_root =~ s%/$%%;

  my $url;
  if( defined( $directory ) ) {
    $url = "$supplement_root/$directory/$name";
  }
  else {
    $url = "$supplement_root/$name";
  }

  d( "Going to fetch $url" );

  my $meta = read_meta( $directory, $name );
  my $cached = read_cache( $directory, $name );

  my %p;

  if( defined( $meta->{Url} ) and ($meta->{Url} eq $url ) ) {
    # The right url is stored in the cache.

    if( defined( $cached ) and defined( $meta->{'LastUpdated'} ) 
	and 1*60*60 > (time - $meta->{'LastUpdated'} ) ) {
      d("LastUpdated ok. Using cache.");
      return $cached;
    }

    if( defined( $cached ) ) {
      $p{'If-Modified-Since'} = $meta->{'Last-Modified'} 
      if defined $meta->{'Last-Modified'};
      $p{'If-None-Match'} = $meta->{ETag}
      if defined $meta->{ETag};
    }
  }

  my $resp = $ua->get( $url, %p );

  if( $resp->code == RC_NOT_MODIFIED ) {
    write_meta( $directory, $url, $name, $resp, $meta );
    d("Not Modified. Using cache.");
    return $cached;
  }
  elsif( $resp->is_success ) {
    write_meta( $directory, $url, $name, $resp, $meta );
    write_cache( $directory, $name, $resp );
    d("Cache miss.");
    return $resp->content;
  }
  elsif( defined( $cached ) ) {
    print STDERR "XMLTV::Supplement: Failed to fetch $url: " .
	$resp->status_line . ". Using cached info.\n";
    return $cached;
  }
  else {
    print STDERR "XMLTV::Supplement: Failed to fetch $url: " . 
	$resp->status_line . ".\n";
    exit 1;
  }
}

sub write_meta {
  my( $directory, $url, $file, $resp, $meta ) = @_;

  my $metafile = cache_filename( $directory, "$file.meta" );

  open OUT, "> $metafile" or die "Failed to write to $metafile";
  print OUT "LastUpdated " . time() . "\n";

  print OUT "Url $url\n";

  if( defined $resp->header( 'Last-Modified' ) ) { 
    print OUT "Last-Modified " . $resp->header( 'Last-Modified' ) . "\n";
  }
  elsif( defined $meta->{'Last-Modified'} ) {
    print OUT "Last-Modified " . $meta->{ 'Last-Modified' } . "\n";
  }

  print OUT "ETag " . $resp->header( 'ETag' )  . "\n"
      if defined $resp->header( 'ETag' );
  close( OUT );
}

sub read_meta {
  my( $directory, $file ) = @_;

  my $metafile = cache_filename( $directory, "$file.meta" );

  return {} if not -f( $metafile );

  my $str = read_file( $metafile );
  my @lines = split( "\n", $str );
  my $result = {};
  foreach my $line (@lines) {
    my($key, $value ) = ($line =~ /(.*?) (.*)/);
    $result->{$key} = $value;
  }

  return $result;
}

sub read_cache {
  my( $directory, $file ) = @_;

  my $filename = cache_filename( $directory, $file );

  my $result;
  eval { $result = read_file( $filename ) };
  return $result;
}

sub write_cache {
  my( $directory, $file, $resp ) = @_;

  my $filename = cache_filename( $directory, $file );

  open OUT, "> $filename" or die "Failed to write to $filename";
  binmode OUT;
  print OUT $resp->content;
  close( OUT );
}

sub cache_filename {
  my( $directory, $file ) = @_;

  if( defined( $directory ) ) {
    return File::Spec->catfile( $cachedir, $directory, $file );
  }
  else {
    return File::Spec->catfile( $cachedir, $file );
  }
}


=item SetSupplementRoot

Set the root directory for loading supplementary files.

    SetSupplementRoot( '/usr/share/xmltv' );
    SetSupplementRoot( 'http://my.server.org/xmltv' );

=cut

sub SetSupplementRoot {
  my( $root ) = @_;

  $supplement_root = $root;
}

=back 4

=head1 CACHING

The module stores all downloaded files in a cache. The cache is stored
on disk in ~/.xmltv/supplement on Unix and in
CSIDL_LOCAL_APPDATA//xmltv/supplement on Windows.

If a file has been downloaded less than 1 hour ago, the file from
the cache is used without contacting the server. Otherwise, if the
file has been downloaded more than 1 hour ago, then the module
checks with the server to see if an updated file is available and
downloads it if necessary.

If the server does not respond or returns an error-message, a warning
is printed to STDERR and the file from the cache is used.

=head1 ENVIRONMENT VARIABLES

The XMLTV_SUPPLEMENT environment variable can be used to tell the
module where the supplementary files are found. 

  XMLTV_SUPPLEMENT=/usr/share/xmltv
  XMLTV_SUPPLEMENT=http://supplementary.xmltv.se

The XMLTV_SUPPLEMENT_VERBOSE environment variable can be used to get
more debug output from XMLTV::Supplement.

  XMLTV_SUPPLEMENT_VERBOSE=1

=head1 COPYRIGHT

Copyright (C) 2007 Mattias Holmlund.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=cut

1;
