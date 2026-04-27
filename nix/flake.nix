{
  description = "Nix environment for Visualising Data Structures (Java 25)";

  inputs = {
    # Using unstable to ensure openjdk25 is available
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, utils }:
    utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        # The 'Driver' JDK (Matches Gradle/Kotlin internal compatibility)
        jdk21 = pkgs.openjdk21;
        
        # The 'Target' JDK (Your project requirements)
        jdk25 = pkgs.openjdk25;

        # Custom Gradle package that strictly uses JDK 21 to run the daemon
        customGradle = pkgs.gradle.override {
          java = jdk21;
        };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            customGradle
            jdk21
            jdk25
          ];

          shellHook = ''
            # Ensure the Gradle Wrapper picks up JDK 21 by default
            export JAVA_HOME=${jdk21}
            
            # Add JDK 25 to the path, but keep 21 as the primary 'java' command
            export PATH="${jdk21}/bin:${jdk25}/bin:$PATH"

            # Export a variable for your local gradle.properties override
            # This tells Gradle exactly where to find the 'Toolchain' JDK 25
            export JDK25_PATH="${jdk25}"

            # This alias ensures that on YOUR machine, gradle always knows where to look
            alias gradle='gradle -Porg.gradle.java.installations.paths=${jdk25} -Porg.gradle.java.installations.auto-download=false'

            # Prompt Customization
            export PS1="\n\[\033[1;34m\][nix-develop]\[\033[0m\] \w > "
            
            echo "❄️  Nix Environment Active"
            echo "❄️  Type 'gradle' to run with Nix-provided Java 25"
          '';
        };
      });
}
