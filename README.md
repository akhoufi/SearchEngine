# SearchEngine

Etapes du moteur de recherche : 
- Construction des fichiers inverses par paquets (par jour par exemple)
- Fusion des fichiers inverses pour construire le fichier inverse (index) de toutes les pages.

Pour une requete donnée : 
- A l'aide de l'index trouver les pages contenant les mots clés de la requete, ce qui va construire un nouveau corpus 
- Pour chacune de ces pages de ce nouveau corpus, calculer le poids des termes présents dans un fichier .poids 
- Calculer la similarité entre la requete (qu'on peut considérer comme un document ??) et les documents de ce corpus (à l'aide des .poids) ??
- Choisir les pages les plus similaires à afficher
- Faire la liaison entre les noms de hashage et le nom de ces pages pour les afficher

