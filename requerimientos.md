# Requerimientos

- Programa debe permitir a usuarios almacenar y gestionar contraseñas de forma segura en línea de comandos.
- Las opciones permitidas son: agregar una contraseña, recuperar (poder visualizarla), actualizarla y eliminarla.
- Considerar información adicional para cada contraseña de manera que el usuario la pueda encontrar facilmente (ej: palabra clave).
- Incluir un módulo de “generador de contraseñas” que genere contraseñas seguras y las muestre en línea de comandos.
- Las contraseñas deben encriptarse y desencriptarse usando un algoritmo bidireccional.
- Usar una clave maestra para acceder a las demás contraseñas (que como mínimo debe ser cifrada con un hash MD5).
- Largo de contraseñas a almacenar deben ser de mínimo 8 caracteres.
- La contraseña debe permitir el uso de caracteres especiales.
- Deben poder existir múltiples usuarios en el sistema.
- Solo indicando la palabra clave se pueden ejecutar las acciones sobre la contraseña.
- Este generador debe permitir que el usuario especifique la longitud y los caracteres permitidos.
