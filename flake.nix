{
  description = "Nix environment for Visualising Data Structures (Java 25)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, utils }:
    utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        jdk21 = pkgs.openjdk21;
        jdk25 = pkgs.openjdk25;
        customGradle = pkgs.gradle.override { java = jdk21; };

        # Define the run script as a package
        runVisualiser = pkgs.writeShellScriptBin "visualiser" ''
          export JAVA_HOME=${jdk21}
          ${customGradle}/bin/gradle \
            -Porg.gradle.java.installations.paths=${jdk25} \
            -Porg.gradle.java.installations.auto-download=false \
            run "$@"
        '';
      in
      {
        # This allows 'nix run'
        packages.default = runVisualiser;

        # This allows 'nix develop'
        devShells.default = pkgs.mkShell {
          buildInputs = [
            customGradle
            jdk21
            jdk25
          ];

          shellHook = ''
            export JAVA_HOME=${jdk21}
            export PATH="${jdk21}/bin:${jdk25}/bin:$PATH"
            export JDK25_PATH="${jdk25}"

            alias gradle='gradle -Porg.gradle.java.installations.paths=${jdk25} -Porg.gradle.java.installations.auto-download=false'

            echo "❄️  Nix Environment Active"
            echo "❄️  Type 'gradle' to build or 'nix run' to execute"
          '';
        };
      });
}
