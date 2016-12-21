# MediaBackup

Ce programme multi-platefome est un utilitaire permettant de créer de trier, et regrouper les photos automatiques. 
MediaBackup va analyser un répertoire contenant des photos et va les copier de manière inteligente dans un répertoire de destination.

Fonctionnalités :

* Trie par date de création (information stockée dans l'image)
 * par année, mois
 * par année, mois, jour
* Tagué vos dates périodiques via un fichier de paramètrage
* Conservation de la structure de votre répertoire de départ
* Regroupe vos images par lot de taille variable

### A quoi peut servir ce programme ?
* Trier une masse de photos par ordre chronologique
* Créer des CD / DVD de sauvegarde en faisant des lots de photos

### Installation
* Installer la version de Java 8
* Télécharger ici le programme

### Configuration
Syntaxe : java [-options] -jar xxxxx.jar

* où options :
    *    -D<name><value>
        *           blocsize        : taille en octet des lots
        *           from            : chemin absolu vers le répertoire source à analyser
        *           to              : chemin absolu vers le répertoire destination
        *           byDay           : true / false regrouper les images par année/mois/jour (si non, cela est par année/mois)

