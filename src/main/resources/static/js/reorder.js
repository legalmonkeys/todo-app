/**
 * Drag and drop reordering functionality for todo items
 */
class ItemReorder {
    constructor() {
        this.init();
    }

    init() {
        this.setupDragAndDrop();
        this.getListId();
    }

    getListId() {
        // Extract list ID from URL or data attribute
        const pathParts = window.location.pathname.split('/');
        const listIndex = pathParts.indexOf('lists');
        if (listIndex !== -1 && pathParts[listIndex + 1]) {
            this.listId = pathParts[listIndex + 1];
        } else {
            console.warn('Unable to determine list ID');
        }
    }

    setupDragAndDrop() {
        const items = document.querySelectorAll('.item[draggable="true"]');

        items.forEach(item => {
            item.addEventListener('dragstart', this.handleDragStart.bind(this));
            item.addEventListener('dragover', this.handleDragOver.bind(this));
            item.addEventListener('drop', this.handleDrop.bind(this));
            item.addEventListener('dragenter', this.handleDragEnter.bind(this));
            item.addEventListener('dragleave', this.handleDragLeave.bind(this));
            item.addEventListener('dragend', this.handleDragEnd.bind(this));
        });
    }


    handleDragStart(e) {
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/html', e.target.outerHTML);
        e.target.classList.add('dragging');
        this.draggedElement = e.target;
    }

    handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
    }

    handleDragEnter(e) {
        e.preventDefault();
        if (e.target.classList.contains('item') && e.target !== this.draggedElement) {
            e.target.classList.add('drag-over');
        }
    }

    handleDragLeave(e) {
        if (e.target.classList.contains('item')) {
            e.target.classList.remove('drag-over');
        }
    }

    handleDrop(e) {
        e.preventDefault();
        e.target.classList.remove('drag-over');

        if (e.target.classList.contains('item') && e.target !== this.draggedElement) {
            // Get all items and determine new order
            const container = document.querySelector('#items-container > div');
            const items = Array.from(container.querySelectorAll('.item'));

            // Find positions
            const draggedIndex = items.indexOf(this.draggedElement);
            const targetIndex = items.indexOf(e.target);

            if (draggedIndex !== targetIndex) {
                // Reorder DOM elements
                if (draggedIndex < targetIndex) {
                    container.insertBefore(this.draggedElement, e.target.nextSibling);
                } else {
                    container.insertBefore(this.draggedElement, e.target);
                }

                // Send reorder request
                this.sendReorderRequest();
            }
        }
    }

    handleDragEnd(e) {
        e.target.classList.remove('dragging');
        // Remove all drag-over classes
        document.querySelectorAll('.drag-over').forEach(el => {
            el.classList.remove('drag-over');
        });
        this.draggedElement = null;
    }


    sendReorderRequest() {
        if (!this.listId) {
            console.error('List ID not found, cannot reorder');
            return;
        }

        // Get current order of items
        const items = document.querySelectorAll('.item[data-item-id]');
        const itemIds = Array.from(items).map(item => {
            const id = item.getAttribute('data-item-id');
            // Handle both UUID strings and numeric IDs
            return isNaN(id) ? id : parseInt(id);
        });

        // Show loading state
        this.showLoadingState(true);

        // Send API request
        fetch(`/api/lists/${this.listId}/items/reorder`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                itemIds: itemIds
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                console.log('Items reordered successfully');
            })
            .catch(error => {
                console.error('Failed to reorder items:', error);
                alert('Failed to save new order. Please refresh the page.');
                // Refresh page to restore original order
                setTimeout(() => window.location.reload(), 1000);
            })
            .finally(() => {
                this.showLoadingState(false);
            });
    }

    showLoadingState(isLoading) {
        const items = document.querySelectorAll('.item');
        items.forEach(item => {
            if (isLoading) {
                item.style.opacity = '0.6';
                item.style.pointerEvents = 'none';
            } else {
                item.style.opacity = '';
                item.style.pointerEvents = '';
            }
        });
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ItemReorder();
});
