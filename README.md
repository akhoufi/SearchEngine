# SearchEngine

Etapes du moteur de recherche : 
- Construction des fichiers inverses par paquets (par jour par exemple)
- Fusion des fichiers inverses pour construire le fichier inverse (index) de toutes les pages.
- Construction des fichiers .poids de toutes les pages (les mettre dans weights/ par exemple)

Pour une requete donnée : 
- Considérer la requete comme un document et calculer son fichier .poids
- Calculer la similarité entre la requete et tout les documents (à l'aide des .poids)
- Choisir les pages les plus similaires à afficher
- Faire la liaison entre les noms de hashage et le nom de ces pages pour les afficher

