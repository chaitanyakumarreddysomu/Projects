document.addEventListener('DOMContentLoaded', (event) => {
				    // Show all toasts
				    const toasts = document.querySelectorAll('#toast-container .toast');
				    
				    toasts.forEach(toast => {
				        toast.classList.add('show');
				        setTimeout(() => {
				            toast.classList.remove('show');
				            toast.classList.add('hide');
				            setTimeout(() => {
				                toast.remove(); // Remove from DOM after hide animation
				            }, 500); // Match the CSS transition duration
				        }, 3000); // Show toast for 3 seconds
				    });
				});